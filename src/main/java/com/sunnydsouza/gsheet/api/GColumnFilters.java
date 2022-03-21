package com.sunnydsouza.gsheet.api;

/*
 * @created 21/03/2022 - 5:01 PM
 * @author sunnydsouza
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A filter condition for a column in the google sheet range (with headers)
 *
 * @author sunnydsouza
 */
public class GColumnFilters {
  private String columnName;
  static Map<String, Predicate> predicateMap = new HashMap<>();

  Logger logger = LoggerFactory.getLogger(GColumnFilters.class);

  private GColumnFilters(String columnName) {
    this.columnName = columnName;
  }

  public static GColumnFilters onCol(String columnName) {
    return new GColumnFilters(columnName);
  }

  public GColumnFilters conditions(Predicate predicate) {
    predicateMap.put(columnName, predicate);
    return this;
  }

  public Predicate<? super Map<String, String>> apply() {
    return r ->
        (r.entrySet().stream()
            .allMatch(m -> predicateMap.getOrDefault(m.getKey(), n -> true).test(m.getValue())));
  }


}
