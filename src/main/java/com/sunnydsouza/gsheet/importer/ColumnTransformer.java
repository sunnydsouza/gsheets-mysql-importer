package com.sunnydsouza.gsheet.importer;

/**
 * This interface defines the contract for transforming a column value.
 * @author sunnydsouza
 * @param <T> The type of the column value after transform ex: DateTime etc..
 */
public interface ColumnTransformer<T> {
    T transform(String value);
}
