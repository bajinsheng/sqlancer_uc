package sqlancer.tidb;

import java.util.ArrayList;
import java.util.List;

import sqlancer.common.query.ExpectedErrors;

public final class TiDBErrors {

    private TiDBErrors() {
    }

    public static List<String> getExpressionErrors() {
        ArrayList<String> errors = new ArrayList<>();

        errors.add("DECIMAL value is out of range");
        errors.add("error parsing regexp");
        errors.add("BIGINT UNSIGNED value is out of range");
        errors.add("Data truncation: Truncated incorrect time value");
        errors.add("Data truncation: Incorrect time value");
        errors.add("Data truncation: Incorrect datetime value");
        errors.add("overflows double");
        errors.add("overflows bigint");
        errors.add("strconv.ParseFloat: parsing");
        errors.add("in 'order clause'"); // int constants in order by clause

        // functions
        errors.add("BIGINT value is out of range");
        errors.add("doesn't have a default value"); // default
        errors.add("is not valid for CHARACTER SET");
        errors.add("DOUBLE value is out of range");
        errors.add("Result of space() was larger than max_allowed_packet");

        errors.add("Data truncat");
        errors.add("Truncated incorrect FLOAT value");
        errors.add("Bad Number");
        errors.add("strconv.Atoi: parsing");
        errors.add("expected integer");
        errors.add("Duplicate entry");

        // regex
        errors.add("error parsing regexp");
        errors.add("from regexp");
        errors.add("Empty pattern is invalid");
        errors.add("Invalid regexp pattern");
        errors.add("Unknown column");

        // To avoid incomplete TiFlash replication
        errors.add("different from the tiflash storage schema");

        if (TiDBBugs.bug35677) {
            errors.add("for function inet_aton");
        }
        if (TiDBBugs.bug35522) {
            errors.add("ERROR 1054 (42S22)");
        }
        if (TiDBBugs.bug35652) {
            errors.add("from binary to utf8");
        }
        if (TiDBBugs.bug38295) {
            errors.add("assertion failed");
        }
        if (TiDBBugs.bug44747) {
            errors.add("index out of range");
        }

        return errors;
    }

    public static void addExpressionErrors(ExpectedErrors errors) {
        errors.addAll(getExpressionErrors());
    }

    public static List<String> getExpressionHavingErrors() {
        ArrayList<String> errors = new ArrayList<>();

        errors.add("is not in GROUP BY clause and contains nonaggregated column");
        errors.add("Unknown column");

        return errors;
    }

    public static void addExpressionHavingErrors(ExpectedErrors errors) {
        errors.addAll(getExpressionHavingErrors());
    }

    public static List<String> getInsertErrors() {
        ArrayList<String> errors = new ArrayList<>();

        errors.add("Duplicate entry");
        errors.add("cannot be null");
        errors.add("doesn't have a default value");
        errors.add("Out of range value");
        errors.add("Incorrect double value");
        errors.add("Incorrect float value");
        errors.add("Incorrect int value");
        errors.add("Incorrect tinyint value");
        errors.add("Data truncation");
        errors.add("Bad Number");
        errors.add("The value specified for generated column"); // TODO: do not insert data into generated columns
        errors.add("incorrect utf8 value");
        errors.add("Data truncation: %s value is out of range in '%s'");
        errors.add("Incorrect smallint value");
        errors.add("Incorrect bigint value");
        errors.add("Incorrect decimal value");
        errors.add("error parsing regexp");
        errors.add("is not valid for CHARACTER SET");
        errors.add("for function inet_aton");
        errors.add("'Empty pattern is invalid' from regexp");

        return errors;
    }

    public static void addInsertErrors(ExpectedErrors errors) {
        errors.addAll(getInsertErrors());
    }

}
