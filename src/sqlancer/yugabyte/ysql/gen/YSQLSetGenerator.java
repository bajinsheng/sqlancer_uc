package sqlancer.yugabyte.ysql.gen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.SQLQueryAdapter;
import sqlancer.yugabyte.ysql.YSQLGlobalState;

public final class YSQLSetGenerator {

    private YSQLSetGenerator() {
    }

    public static SQLQueryAdapter create(YSQLGlobalState globalState) {
        StringBuilder sb = new StringBuilder();
        ArrayList<ConfigurationOption> options = new ArrayList<>(Arrays.asList(ConfigurationOption.values()));
        options.remove(ConfigurationOption.DEFAULT_WITH_OIDS);
        ConfigurationOption option = Randomly.fromList(options);
        sb.append("SET ");
        if (Randomly.getBoolean()) {
            sb.append(Randomly.fromOptions("SESSION", "LOCAL"));
            sb.append(" ");
        }
        sb.append(option.getOptionName());
        sb.append("=");
        if (Randomly.getBoolean()) {
            sb.append("DEFAULT");
        } else {
            sb.append(option.op.apply(globalState.getRandomly()));
        }
        // todo avoiding props that are not represented in YSQL
        ExpectedErrors errors = new ExpectedErrors();
        errors.add("unrecognized configuration parameter");
        errors.add("cannot be changed");

        return new SQLQueryAdapter(sb.toString(), errors);
    }

    private enum ConfigurationOption {
        // YUGABYTE
        YB_DEBUG_REPORT_ERROR_STACKTRACE("yb_debug_report_error_stacktrace",
                (r) -> Randomly.fromOptions("false", "true")),
        YB_DEBUG_LOG_CATCACHE_EVENTS("yb_debug_log_catcache_events", (r) -> Randomly.fromOptions("false", "true")),
        YB_DEBUG_LOG_INTERNAL_RESTARTS("yb_debug_log_internal_restarts", (r) -> Randomly.fromOptions("false", "true")),
        YB_DEBUG_LOG_DOCDB_REQUESTS("yb_debug_log_docdb_requests", (r) -> Randomly.fromOptions("false", "true")),
        // YB_READ_FROM_FOLLOWERS("yb_read_from_followers", (r) -> Randomly.fromOptions("false", "true")),
        YB_NON_DDL_TXN_FOR_SYS_TABLES_ALLOWED("yb_non_ddl_txn_for_sys_tables_allowed",
                (r) -> Randomly.fromOptions("false", "true")),
        YB_TRANSACTION_PRIORITY("yb_transaction_priority",
                (r) -> Randomly.fromOptions(0, 0.1, 0.2, 0.3, 0.4, 1, 0.9, 0.8, 0.7, 0.6)),
        YB_TRANSACTION_PRIORITY_LOWER_BOUND("yb_transaction_priority_lower_bound",
                (r) -> Randomly.fromOptions(0, 0.1, 0.2, 0.3, 0.4)),
        YB_TRANSACTION_PRIORITY_UPPER_BOUND("yb_transaction_priority_upper_bound",
                (r) -> Randomly.fromOptions(1, 0.9, 0.8, 0.7, 0.6)),
        YB_FORMAT_FUNCS_INCLUDE_YB_METADATA("yb_format_funcs_include_yb_metadata",
                (r) -> Randomly.fromOptions("false", "true")),
        YB_ENABLE_GEOLOCATION_COSTING("yb_enable_geolocation_costing", (r) -> Randomly.fromOptions("false", "true")),
        YB_BINARY_RESTORE("yb_binary_restore", (r) -> Randomly.fromOptions("false", "true")),
        YB_TEST_SYSTEM_CATALOGS_CREATION("yb_test_system_catalogs_creation",
                (r) -> Randomly.fromOptions("false", "true")),
        YB_TEST_FAIL_NEXT_DDL("yb_test_fail_next_ddl", (r) -> Randomly.fromOptions("false", "true")),
        YB_ENABLE_OPTIMIZER_STATISTICS("yb_enable_optimizer_statistics", (r) -> Randomly.fromOptions("false", "true")),
        YB_ENABLE_EXPRESSION_PUSHDOWN("yb_enable_expression_pushdown", (r) -> Randomly.fromOptions("false", "true")),
        YB_ENABLE_UPSERT_MODE("yb_enable_upsert_mode", (r) -> Randomly.fromOptions("false", "true")),
        YB_PLANNER_CUSTOM_PLAN_FOR_PARTITION_PRUNING("yb_planner_custom_plan_for_partition_pruning",
                (r) -> Randomly.fromOptions("false", "true")),
        YB_INDEX_STATE_FLAGS_UPDATE_DELAY("yb_index_state_flags_update_delay",
                (r) -> Randomly.getNotCachedInteger(200, 1000)),
        YB_TEST_PLANNER_CUSTOM_PLAN_THRESHOLD("yb_test_planner_custom_plan_threshold",
                (r) -> Randomly.getNotCachedInteger(1, Integer.MAX_VALUE)),
        // YSQL values
        YSQL_UPGRADE_MODE("ysql_upgrade_mode", (r) -> Randomly.fromOptions("false", "true")),
        YSQL_SESSION_MAX_BATCH_SIZE("ysql_session_max_batch_size",
                (r) -> Randomly.getNotCachedInteger(1, Integer.MAX_VALUE)),
        YSQL_MAX_IN_FLIGHT_OPS("ysql_max_in_flight_ops", (r) -> Randomly.getNotCachedInteger(1, Integer.MAX_VALUE)),
        // https://www.postgresql.org/docs/11/runtime-config-wal.html
        // This parameter can only be set at server start.
        // WAL_LEVEL("wal_level", (r) -> Randomly.fromOptions("replica", "minimal", "logical")),
        // FSYNC("fsync", (r) -> Randomly.fromOptions(1, 0)),
        SYNCHRONOUS_COMMIT("synchronous_commit",
                (r) -> Randomly.fromOptions("remote_apply", "remote_write", "local", "off")),
        WAL_COMPRESSION("wal_compression", (r) -> Randomly.fromOptions(1, 0)),
        // wal_buffer: server start
        // wal_writer_delay: server start
        // wal_writer_flush_after
        COMMIT_DELAY("commit_delay", (r) -> r.getInteger(0, 100000)),
        COMMIT_SIBLINGS("commit_siblings", (r) -> r.getInteger(0, 1000)),
        // 19.5.2. Checkpoints
        // checkpoint_timeout
        // checkpoint_completion_target
        // checkpoint_flush_after
        // checkpoint_warning
        // max_wal_size
        // min_wal_size
        // 19.5.3. Archiving
        // archive_mode
        // archive_command
        // archive_timeout
        // https://www.postgresql.org/docs/11/runtime-config-statistics.html
        // 19.9.1. Query and Index Statistics Collector
        TRACK_ACTIVITIES("track_activities", (r) -> Randomly.fromOptions(1, 0)),
        // track_activity_query_size
        TRACK_COUNTS("track_counts", (r) -> Randomly.fromOptions(1, 0)),
        TRACK_IO_TIMING("track_io_timing", (r) -> Randomly.fromOptions(1, 0)),
        TRACK_FUNCTIONS("track_functions", (r) -> Randomly.fromOptions("'none'", "'pl'", "'all'")),
        // stats_temp_directory
        // TODO 19.9.2. Statistics Monitoring
        // https://www.postgresql.org/docs/11/runtime-config-autovacuum.html
        // all can only be set at server-conf time
        // 19.11. Client Connection Defaults
        VACUUM_FREEZE_TABLE_AGE("vacuum_freeze_table_age", (r) -> Randomly.fromOptions(0, 5, 10, 100, 500, 2000000000)),
        VACUUM_FREEZE_MIN_AGE("vacuum_freeze_min_age", (r) -> Randomly.fromOptions(0, 5, 10, 100, 500, 1000000000)),
        VACUUM_MULTIXACT_FREEZE_TABLE_AGE("vacuum_multixact_freeze_table_age",
                (r) -> Randomly.fromOptions(0, 5, 10, 100, 500, 2000000000)),
        VACUUM_MULTIXACT_FREEZE_MIN_AGE("vacuum_multixact_freeze_min_age",
                (r) -> Randomly.fromOptions(0, 5, 10, 100, 500, 1000000000)),
        VACUUM_CLEANUP_INDEX_SCALE_FACTOR("vacuum_cleanup_index_scale_factor",
                (r) -> Randomly.fromOptions(0.0, 0.0000001, 0.00001, 0.01, 0.1, 1, 10, 100, 100000, 10000000000.0)),
        // TODO others
        GIN_FUZZY_SEARCH_LIMIT("gin_fuzzy_search_limit", (r) -> r.getInteger(0, 2147483647)),
        // 19.13. Version and Platform Compatibility
        DEFAULT_WITH_OIDS("default_with_oids", (r) -> Randomly.fromOptions(0, 1)),
        SYNCHRONIZED_SEQSCANS("synchronize_seqscans", (r) -> Randomly.fromOptions(0, 1)),
        // https://www.postgresql.org/docs/devel/runtime-config-query.html
        ENABLE_BITMAPSCAN("enable_bitmapscan", (r) -> Randomly.fromOptions(1, 0)),
        ENABLE_GATHERMERGE("enable_gathermerge", (r) -> Randomly.fromOptions(1, 0)),
        ENABLE_HASHJOIN("enable_hashjoin", (r) -> Randomly.fromOptions(1, 0)),
        ENABLE_INDEXSCAN("enable_indexscan", (r) -> Randomly.fromOptions(1, 0)),
        ENABLE_INDEXONLYSCAN("enable_indexonlyscan", (r) -> Randomly.fromOptions(1, 0)),
        ENABLE_MATERIAL("enable_material", (r) -> Randomly.fromOptions(1, 0)),
        ENABLE_MERGEJOIN("enable_mergejoin", (r) -> Randomly.fromOptions(1, 0)),
        ENABLE_NESTLOOP("enable_nestloop", (r) -> Randomly.fromOptions(1, 0)),
        ENABLE_PARALLEL_APPEND("enable_parallel_append", (r) -> Randomly.fromOptions(1, 0)),
        ENABLE_PARALLEL_HASH("enable_parallel_hash", (r) -> Randomly.fromOptions(1, 0)),
        ENABLE_PARTITION_PRUNING("enable_partition_pruning", (r) -> Randomly.fromOptions(1, 0)),
        ENABLE_PARTITIONWISE_JOIN("enable_partitionwise_join", (r) -> Randomly.fromOptions(1, 0)),
        ENABLE_PARTITIONWISE_AGGREGATE("enable_partitionwise_aggregate", (r) -> Randomly.fromOptions(1, 0)),
        ENABLE_SEGSCAN("enable_seqscan", (r) -> Randomly.fromOptions(1, 0)),
        ENABLE_SORT("enable_sort", (r) -> Randomly.fromOptions(1, 0)),
        ENABLE_TIDSCAN("enable_tidscan", (r) -> Randomly.fromOptions(1, 0)),
        // 19.7.2. Planner Cost Constants (complete as of March 2020)
        // https://www.postgresql.org/docs/current/runtime-config-query.html#RUNTIME-CONFIG-QUERY-CONSTANTS
        SEQ_PAGE_COST("seq_page_cost", (r) -> Randomly.fromOptions(0d, 0.00001, 0.05, 0.1, 1, 10, 10000)),
        RANDOM_PAGE_COST("random_page_cost", (r) -> Randomly.fromOptions(0d, 0.00001, 0.05, 0.1, 1, 10, 10000)),
        CPU_TUPLE_COST("cpu_tuple_cost", (r) -> Randomly.fromOptions(0d, 0.00001, 0.05, 0.1, 1, 10, 10000)),
        CPU_INDEX_TUPLE_COST("cpu_index_tuple_cost", (r) -> Randomly.fromOptions(0d, 0.00001, 0.05, 0.1, 1, 10, 10000)),
        CPU_OPERATOR_COST("cpu_operator_cost", (r) -> Randomly.fromOptions(0d, 0.000001, 0.0025, 0.1, 1, 10, 10000)),
        PARALLEL_SETUP_COST("parallel_setup_cost", (r) -> r.getLong(0, Long.MAX_VALUE)),
        PARALLEL_TUPLE_COST("parallel_tuple_cost", (r) -> r.getLong(0, Long.MAX_VALUE)),
        MIN_PARALLEL_TABLE_SCAN_SIZE("min_parallel_table_scan_size", (r) -> r.getInteger(0, 715827882)),
        MIN_PARALLEL_INDEX_SCAN_SIZE("min_parallel_index_scan_size", (r) -> r.getInteger(0, 715827882)),
        EFFECTIVE_CACHE_SIZE("effective_cache_size", (r) -> r.getInteger(1, 2147483647)),
        JIT_ABOVE_COST("jit_above_cost", (r) -> Randomly.fromOptions(0, r.getLong(-1, Long.MAX_VALUE - 1))),
        JIT_INLINE_ABOVE_COST("jit_inline_above_cost", (r) -> Randomly.fromOptions(0, r.getLong(-1, Long.MAX_VALUE))),
        JIT_OPTIMIZE_ABOVE_COST("jit_optimize_above_cost",
                (r) -> Randomly.fromOptions(0, r.getLong(-1, Long.MAX_VALUE))),
        // 19.7.3. Genetic Query Optimizer (complete as of March 2020)
        // https://www.postgresql.org/docs/current/runtime-config-query.html#RUNTIME-CONFIG-QUERY-GEQO
        GEQO("geqo", (r) -> Randomly.fromOptions(1, 0)),
        GEQO_THRESHOLD("geqo_threshold", (r) -> r.getInteger(2, 2147483647)),
        GEQO_EFFORT("geqo_effort", (r) -> r.getInteger(1, 10)),
        GEQO_POO_SIZE("geqo_pool_size", (r) -> r.getInteger(0, 2147483647)),
        GEQO_GENERATIONS("geqo_generations", (r) -> r.getInteger(0, 2147483647)),
        GEQO_SELECTION_BIAS("geqo_selection_bias", (r) -> Randomly.fromOptions(1.5, 1.8, 2.0)),
        GEQO_SEED("geqo_seed", (r) -> Randomly.fromOptions(0, 0.5, 1)),
        // 19.7.4. Other Planner Options (complete as of March 2020)
        // https://www.postgresql.org/docs/current/runtime-config-query.html#RUNTIME-CONFIG-QUERY-OTHER
        DEFAULT_STATISTICS_TARGET("default_statistics_target", (r) -> r.getInteger(1, 10000)),
        CONSTRAINT_EXCLUSION("constraint_exclusion", (r) -> Randomly.fromOptions("on", "off", "partition")),
        CURSOR_TUPLE_FRACTION("cursor_tuple_fraction",
                (r) -> Randomly.fromOptions(0.0, 0.1, 0.000001, 1, 0.5, 0.9999999)),
        FROM_COLLAPSE_LIMIT("from_collapse_limit", (r) -> r.getInteger(1, Integer.MAX_VALUE)),
        JIT("jit", (r) -> Randomly.fromOptions(1, 0)),
        JOIN_COLLAPSE_LIMIT("join_collapse_limit", (r) -> r.getInteger(1, Integer.MAX_VALUE)),
        PARALLEL_LEADER_PARTICIPATION("parallel_leader_participation", (r) -> Randomly.fromOptions(1, 0)),
        FORCE_PARALLEL_MODE("force_parallel_mode", (r) -> Randomly.fromOptions("off", "on", "regress"));

        private final String optionName;
        private final Function<Randomly, Object> op;

        ConfigurationOption(String optionName, Function<Randomly, Object> op) {
            this.optionName = optionName;
            this.op = op;
        }

        public String getOptionName() {
            return optionName;
        }
    }

}
