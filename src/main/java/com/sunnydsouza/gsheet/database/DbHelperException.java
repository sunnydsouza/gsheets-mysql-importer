package com.sunnydsouza.gsheet.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception class for database helper.
 *
 * @author: Sunny Dsouza
 */
public class DbHelperException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private Logger logger = LoggerFactory.getLogger(DbHelperException.class);

    public DbHelperException() {
        super();
    }

    public DbHelperException(Throwable t) {
        super(t);
    }

    public DbHelperException(String message) {
        super(message);
    }

    public DbHelperException(String message, Throwable t) {
        super(message, t);
    }
}
