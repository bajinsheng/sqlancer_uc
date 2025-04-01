#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <unistd.h>
#include <string.h>
#include <semaphore.h>

#define SHM_BARRIER_NAME "/shm_barrier"

typedef struct {
    int threshold;
    sem_t sem;
} shared_barrier_t;

void init_shared_memory(int threshold) {
    int shm_fd = shm_open(SHM_BARRIER_NAME, O_CREAT | O_RDWR, 0666);
    if (shm_fd == -1) {
        perror("shm_open");
        exit(1);
    }

    if (ftruncate(shm_fd, sizeof(shared_barrier_t)) == -1) {
        perror("ftruncate");
        exit(1);
    }

    shared_barrier_t *barrier = mmap(NULL, sizeof(shared_barrier_t), PROT_READ | PROT_WRITE, MAP_SHARED, shm_fd, 0);
    if (barrier == MAP_FAILED) {
        perror("mmap");
        exit(1);
    }

    barrier->threshold = threshold;
    if (sem_init(&barrier->sem, 1, 0) == -1) {
        perror("sem_init counter_1");
        exit(1);
    }

    // Ensure changes are written back
    if (msync(barrier, sizeof(shared_barrier_t), MS_SYNC | MS_INVALIDATE) == -1) {
        perror("msync");
    }

    // Memory barrier to ensure visibility
    __sync_synchronize();

    munmap(barrier, sizeof(shared_barrier_t));
    close(shm_fd);

    printf("Barrier threshold shared memory initialized with value: %d\n", threshold);
}

void status_shared_memory() {
    int shm_fd = shm_open(SHM_BARRIER_NAME, O_RDWR, 0666);
    if (shm_fd == -1) {
        perror("shm_open");
        exit(1);
    }

    shared_barrier_t *barrier = mmap(NULL, sizeof(shared_barrier_t), PROT_READ | PROT_WRITE, MAP_SHARED, shm_fd, 0);
    if (barrier == MAP_FAILED) {
        perror("mmap");
        exit(1);
    }

    int sem_value;
    if (sem_getvalue(&barrier->sem, &sem_value) == -1) {
        perror("sem_getvalue");
        exit(1);
    }
    printf("Shared memory contains:\n"
           "Threshold: %d\n"
           "Semaphore value: %d\n",
           barrier->threshold, sem_value);

    munmap(barrier, sizeof(shared_barrier_t));
    close(shm_fd);
}

void update_shared_memory(int threshold) {
    int shm_fd = shm_open(SHM_BARRIER_NAME, O_RDWR, 0666);
    if (shm_fd == -1) {
        perror("shm_open");
        exit(1);
    }

    shared_barrier_t *barrier = mmap(NULL, sizeof(shared_barrier_t), PROT_READ | PROT_WRITE, MAP_SHARED, shm_fd, 0);
    if (barrier == MAP_FAILED) {
        perror("mmap");
        exit(1);
    }

    barrier->threshold = threshold;

    // Ensure changes are written back
    if (msync(barrier, sizeof(shared_barrier_t), MS_SYNC | MS_INVALIDATE) == -1) {
        perror("msync");
    }

    // Memory barrier to ensure visibility
    __sync_synchronize();

    munmap(barrier, sizeof(shared_barrier_t));
    close(shm_fd);

    printf("Barrier threshold shared memory updated with value: %d\n", threshold);
}

void remove_shared_memory() {
    int shm_fd = shm_open(SHM_BARRIER_NAME, O_RDWR, 0666);
    if (shm_fd == -1) {
        perror("shm_open");
        exit(1);
    }
    shared_barrier_t *barrier = mmap(NULL, sizeof(shared_barrier_t), PROT_READ | PROT_WRITE, MAP_SHARED, shm_fd, 0);
    if (barrier == MAP_FAILED) {
        perror("mmap");
        exit(1);
    }
    if (sem_destroy(&barrier->sem) == -1) {
        perror("sem_destroy");
        exit(1);
    }
    // Ensure changes are written back
    if (msync(barrier, sizeof(shared_barrier_t), MS_SYNC | MS_INVALIDATE) == -1) {
        perror("msync");
    }

    // Memory barrier to ensure visibility
    __sync_synchronize();
    munmap(barrier, sizeof(shared_barrier_t));
    close(shm_fd);
    if (shm_fd == -1) {
        perror("shm_open");
        exit(1);
    }
    if (shm_unlink(SHM_BARRIER_NAME) == -1) {
        perror("shm_unlink");
        exit(1);
    }
    printf("Barrier threshold shared memory removed\n");
}

int main(int argc, char *argv[]) {
    if (argc < 2) {
        fprintf(stderr, "Usage: %s <command> [args]\n", argv[0]);
        fprintf(stderr, "Commands: init, status, update, remove\n");
        exit(EXIT_FAILURE);
    }

    if (strcmp(argv[1], "init") == 0) {
        if (argc != 3) {
            fprintf(stderr, "Usage: %s init <threshold>\n", argv[0]);
            exit(EXIT_FAILURE);
        }
        int threshold = atoi(argv[2]);
        init_shared_memory(threshold);
    } else if (strcmp(argv[1], "status") == 0) {
        status_shared_memory();
    } else if (strcmp(argv[1], "update") == 0) {
        if (argc != 3) {
            fprintf(stderr, "Usage: %s update <threshold>\n", argv[0]);
            exit(EXIT_FAILURE);
        }
        int threshold = atoi(argv[2]);
        update_shared_memory(threshold);
    } else if (strcmp(argv[1], "remove") == 0) {
        remove_shared_memory();
    } else {
        fprintf(stderr, "Unknown command: %s\n", argv[1]);
        exit(EXIT_FAILURE);
    }

    return 0;
}