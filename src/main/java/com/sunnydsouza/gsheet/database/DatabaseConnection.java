package com.sunnydsouza.gsheet.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for handling the Database connection (MySQL/MariaDb/Oracle) using JDBC connection string
 * and JDBC driver (via pom.
 *
 * @author: sunnydsouza
 */
public class DatabaseConnection {

  public static final Map<String, Connection> dbConectionHashMap = new HashMap<>();
  public static final String QUERY_TO_BE_EXECUTED = "Query to be executed ->";
  public static final String ERROR_EXECUTING_SELECT_QUERY = "Error in Executing Select Query->";
  private static Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

  /**
   * Establish a connection with the given connectionString and username and password
   *
   * @param connectionString
   * @param username
   * @param password
   * @return the Connection object. This is also stored in the dbConectionHashMap
   */
  public static Connection getDatabaseConnection(
      String connectionString, String username, String password) {
    try {
      if (dbConectionHashMap.containsKey(connectionString) && isConnectionValid(connectionString)) {
        logger.trace(
            "dbConnectionHashMap contains connectionString -> {}  and the connection is valid",
            connectionString);
        return dbConectionHashMap.get(connectionString);
      } else {
        logger.trace(
            "dbConnectionHashMap either doesnt contains connectionString -> {}  or the connection is not valid",
            connectionString);

        if (dbConectionHashMap.containsKey(connectionString)) {
          dbConectionHashMap.get(connectionString).close();
        }
        dbConectionHashMap.put(
            connectionString, DriverManager.getConnection(connectionString, username, password));
        return dbConectionHashMap.get(connectionString);
      }
    } catch (Exception e) {
      throw new DbHelperException("Error in DB Connection :" + e.getMessage());
    }
  }

  /**
   * Gets the current connection from the dbConectionHashMap based on the connectionString
   *
   * @param connectionString
   * @return the connection object
   */
  public static Connection getCurrentConnection(String connectionString) {
    if (dbConectionHashMap.containsKey(connectionString) && isConnectionValid(connectionString)) {
      logger.trace(
          "dbConnectionHashMap contains connectionString -> {}  and the connection is valid",
          connectionString);
      return dbConectionHashMap.get(connectionString);
    } else {
      throw new DbHelperException(
          "No current connection with "
              + connectionString
              + " available.Please use "
              + "getDatabaseConnection(String connectionString, String username, String password) to establish a "
              + "connection first");
    }
  }

  /**
   * Checks if the given connection is valid
   *
   * @param connectionString
   * @return true/false based on the validity of the connection
   */
  public static boolean isConnectionValid(String connectionString) {
    boolean isConnValid = false;
    String query = null;

    try {

      query = "select '1' from dual";

      try (Statement stmt = dbConectionHashMap.get(connectionString).prepareStatement(query); ) {
        try (ResultSet rs = stmt.executeQuery(query); ) {
          if (rs.next() && ("1").equalsIgnoreCase(rs.getString(1))) {
            isConnValid = true;
          }
        }
      } catch (SQLException e) {
        throw new DbHelperException(ERROR_EXECUTING_SELECT_QUERY + query + "->" + e.getMessage());
      }
    } catch (Exception e) {
      throw new DbHelperException("Error While Checking Connection Validity :" + e.getMessage());
    }

    return isConnValid;
  }

  /**
   * Responsible for executing the query and returning the result set in the form of a list of
   * records(Map<String,Object></String,Object>
   *
   * @param connectionString
   * @param query
   * @return
   * @throws SQLException
   */
  public static List<Map<String, Object>> executeSelectQuery(
      String connectionString, String query) {
    logger.trace(
        "--------------------------- executeSelectQuery Start-----------------------------");
    List<Map<String, Object>> resultRows = new ArrayList<>();
    Map<String, Object> row = null;

    logger.debug(QUERY_TO_BE_EXECUTED + "\n {}", query);

    try (Statement stmt = getCurrentConnection(connectionString).createStatement(); ) {
      try (ResultSet rs = stmt.executeQuery(query); ) {
        // Retrieving the data
        ResultSetMetaData rsmd = rs.getMetaData();
        // getting the column type
        int column_count = rsmd.getColumnCount();
        logger.trace("No. of columns {}", rsmd.getColumnCount());
        while (rs.next()) {
          row = new HashMap<>();
          for (int i = 1; i <= column_count; i++) {
            logger.trace("Column name->{} : Value->{}", rsmd.getColumnName(i), rs.getObject(i));
            row.put(rsmd.getColumnName(i), rs.getObject(i));
          }
          resultRows.add(row);
          logger.trace("Result rows from executing query -> {}", resultRows);
        }
      } catch (SQLException e) {
        throw new DbHelperException(ERROR_EXECUTING_SELECT_QUERY + query + "->" + e.getMessage());
      }
    } catch (SQLException e) {
      throw new DbHelperException(ERROR_EXECUTING_SELECT_QUERY + query + "->" + e.getMessage());
    }

    logger.trace("--------------------------- executeSelectQuery End-----------------------------");
    return resultRows;
  }

  /**
   * Gets the record count for the query executed
   *
   * @param connectionString
   * @param query
   * @return the record count for the query executed
   * @throws SQLException
   */
  public static int getRecordCount(String connectionString, String query) throws SQLException {
    logger.trace(
        "--------------------------- executeCountQuery Start-----------------------------");
    int rows = 0;

    logger.debug(QUERY_TO_BE_EXECUTED);
    logger.debug(query);
    try (Statement stmt = getCurrentConnection(connectionString).createStatement(); ) {
      try (ResultSet rs = stmt.executeQuery(query); ) {

        while (rs.next()) {

          rows = rs.getInt(1);
          logger.debug("Row count from executing query->{}", rows);
        }
      } catch (SQLException e) {
        throw new DbHelperException("Error in Executing Count Query" + e.getMessage());
      }
    } catch (SQLException e) {
      throw new DbHelperException("Error in Executing Count Query" + e.getMessage());
    }

    logger.trace("--------------------------- executeCountQuery End-----------------------------");
    return rows;
  }

  /**
   * Executes a DML or DDL query. The result is a result code (either 0 or 1) based on success of
   * operation
   *
   * @param connectionString
   * @param ddlQuery
   * @return resultCode
   * @deprecated use {@link #executeDmlDdlQuery(PreparedStatement stmt)} instead
   */
  @Deprecated
  public static int executeDmlDdlQuery(String connectionString, String ddlQuery) {
    logger.trace("--------------------------- executeDDLQuery Start -----------------------------");
    int result = -1;

    logger.debug(QUERY_TO_BE_EXECUTED);
    logger.debug(ddlQuery);
    try (Statement stmt = getCurrentConnection(connectionString).createStatement(); ) {

      result = stmt.executeUpdate(ddlQuery);
      logger.debug("Executed DDL query successfully. ENd result->{}", result);
    } catch (SQLException e) {
      throw new DbHelperException("Error in Executing DDL Query" + e.getMessage());
    }
    logger.trace("--------------------------- executeDDLQuery End -----------------------------");
    return result;
  }

  /**
   * Executes a DML or DDL query. The result is a result code (either 0 or 1) based on success of
   * operation This overloaded method takes a Sql Prepared Statement as input. Prefered method to
   * use when the query is dynamic and needs to be executed multiple times.
   *
   * @param stmt
   * @return
   * @throws SQLException
   */
  public static int executeDmlDdlQuery(PreparedStatement stmt) throws SQLException {
    logger.trace(
        "--------------------------- executeDmlDdlQuery Start -----------------------------");
    int result = -1;

    try {
      logger.debug("Executing Sql statement -> {}", stmt);
      result = stmt.executeUpdate();
      logger.debug("Executed DDL query successfully. End result->{}", result);
    } catch (SQLException e) {
      throw new DbHelperException("Error in Executing DDL Query" + e.getMessage());
    } finally {
      stmt.close();
    }
    logger.trace(
        "--------------------------- executeDmlDdlQuery End -----------------------------");
    return result;
  }

  /** Close the DB connection */
  public static void closeDBConnection() {
    try {
      for (Map.Entry<String, Connection> connObjectEntry : dbConectionHashMap.entrySet()) {
        if (!connObjectEntry.getValue().isClosed()) connObjectEntry.getValue().close();
      }
      dbConectionHashMap.clear();
    } catch (Exception e) {

      throw new DbHelperException("Error in Closing the Connection" + e.getMessage());
    }
  }
}
