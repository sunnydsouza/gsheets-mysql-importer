package com.sunnydsouza.gsheet.api;/*
 * @created 21/03/2022 - 7:02 PM
 * @author sunnydsouza
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

public class GCondition {

    static Logger logger = LoggerFactory.getLogger(GCondition.class);

    //GColumnFilters.onCol("RecordedTimestamp").conditions(GCondition.lessThan("30/01/2022").andThen(GCondition.greaterThan("30/01/2022")))
    public static <T> Predicate<T> greaterThan(String expectedValue) {
        //Currently only supporting comparsion as String
        //TODO scope to include numeric and date comparsion also
        T t = null;
        if (t instanceof String) {
            logger.debug("expectedValue: {} is instance of String", expectedValue);
            //Placeholder code. TODO implement this
        } else {
            logger.debug("expectedValue: {} is not instance of String", expectedValue);
        }
        Predicate<T> verifyValueGreaterThan = v -> (((String)v).compareTo(expectedValue) > 0);
        return verifyValueGreaterThan;
    }

    public static <T> Predicate<T> lessThan(String expectedValue) {
        //Currently only supporting comparsion as String
        //TODO scope to include numeric and date comparsion also
        Predicate<T> verifyValueLessThan = v -> (((String) v).compareTo(expectedValue) < 0);
        return verifyValueLessThan;
    }

    public static <T> Predicate<T> lessThanOrEquals(String expectedValue) {
        //Currently only supporting comparsion as String
        //TODO scope to include numeric and date comparsion also
        Predicate<T> verifyValueGreaterThan = v -> (((String) v).compareTo(expectedValue) >= 0);
        return verifyValueGreaterThan;
    }

    public static <T> Predicate<T> greaterThanOrEquals(String expectedValue) {
        //Currently only supporting comparsion as String
        //TODO scope to include numeric and date comparsion also
        Predicate<T> verifyValueLessThan = v -> (((String) v).compareTo(expectedValue) <= 0);
        return verifyValueLessThan;
    }

    public static <T> Predicate<T> equals(String expectedValue) {
        //Currently only supporting comparsion as String
        //TODO scope to include numeric and date comparsion also
        Predicate<T> equals = v -> (v.equals(expectedValue));
        return equals;
    }

    public static <T> Predicate<T> equalsIgnoreCase(String expectedValue) {
        //Currently only supporting comparsion as String
        //TODO scope to include numeric and date comparsion also
        Predicate<T> equalsIgnoreCase = v -> (((String) v).equalsIgnoreCase(expectedValue));
        return equalsIgnoreCase;
    }

    public static <T> Predicate<T> notEquals(String expectedValue) {
        //Currently only supporting comparsion as String
        //TODO scope to include numeric and date comparsion also
        Predicate<T> notEquals = v -> (!((String) v).equalsIgnoreCase(expectedValue));
        return notEquals;
    }

    public static Predicate<String> contains(String expectedValue) {
        return v -> (v.contains(expectedValue));
    }

    public static Predicate<String> notContains(String expectedValue) {
        return v -> (!v.contains(expectedValue));
    }

    public static Predicate<String> startsWith(String expectedValue) {
        return v -> (v.startsWith(expectedValue));
    }

    public static Predicate<String> notStartsWith(String expectedValue) {
        return v -> (!v.startsWith(expectedValue));
    }

    public Predicate<String> endsWith(String expectedValue) {
        return v -> (v.endsWith(expectedValue));
    }

    public Predicate<String> notEndsWith(String expectedValue) {
        return v -> (!v.endsWith(expectedValue));
    }

    public static Predicate<String> in(List<String> expectedValues) {
        return v -> (expectedValues.contains(v));
    }

    public static Predicate<String> notIn(List<String> expectedValues) {
        return v -> (!expectedValues.contains(v));
    }

    public static Predicate<String> isEmpty() {
        return v -> (v.isEmpty());
    }

    public static Predicate<String> isNotEmpty() {
        return v -> (!v.isEmpty());
    }


}
