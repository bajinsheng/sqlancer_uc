#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <unistd.h>
#include <semaphore.h>
#include "sqlancer_jni_UtilSHM.h"
#include <string.h>

#define SHM_BARRIER_NAME "/shm_barrier"
#define SHM_INFO_NAME "/shm_loc"

typedef struct {
    int threshold;
    sem_t sem;
} shared_barrier_t;

// Alternatively, re-implement the JNI bridge functions using their logic inline:

JNIEXPORT void JNICALL Java_sqlancer_jni_UtilSHM_initBarrier
  (JNIEnv *env, jobject obj, jint threshold) {
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
}

JNIEXPORT void JNICALL Java_sqlancer_jni_UtilSHM_updateBarrier
  (JNIEnv *env, jobject obj, jint threshold) {
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
}



typedef struct {
    int pid_1;
    int loc_1;
    sem_t counter_1;
    int target_counter_1;
    int pid_2;
    int loc_2;
    sem_t counter_2;
    int target_counter_2;
} shared_info_t;

JNIEXPORT void JNICALL Java_sqlancer_jni_UtilSHM_initLoc
  (JNIEnv *env, jobject obj, jint pid_1, jint loc_1, jint target_counter_1, jint pid_2, jint loc_2, jint target_counter_2) {
    int shm_fd = shm_open(SHM_INFO_NAME, O_CREAT | O_RDWR, 0666);
    if (shm_fd == -1) {
        perror("shm_open");
        exit(1);
    }

    if (ftruncate(shm_fd, sizeof(shared_info_t)) == -1) {
        perror("ftruncate");
        exit(1);
    }

    shared_info_t *info = mmap(NULL, sizeof(shared_info_t), PROT_READ | PROT_WRITE, MAP_SHARED, shm_fd, 0);
    if (info == MAP_FAILED) {
        perror("mmap");
        exit(1);
    }

    info->pid_1 = pid_1;
    info->loc_1 = loc_1;
    info->target_counter_1 = target_counter_1;
    info->pid_2 = pid_2;
    info->loc_2 = loc_2;
    info->target_counter_2 = target_counter_2;
    
    // Initialize semaphores (shared=1, initial value=0)
    if (sem_init(&info->counter_1, 1, 0) == -1) {
        perror("sem_init counter_1");
        exit(1);
    }
    
    if (sem_init(&info->counter_2, 1, 0) == -1) {
        perror("sem_init counter_2");
        exit(1);
    }
    // Ensure changes are written back
    if (msync(info, sizeof(shared_info_t), MS_SYNC | MS_INVALIDATE) == -1) {
        perror("msync");
    }

    // Memory barrier to ensure visibility
    __sync_synchronize();
    munmap(info, sizeof(shared_info_t));
    close(shm_fd);
}