package com.sunnydsouza.gsheet.importer;

import com.sunnydsouza.gsheet.TableTransformer;
import com.sunnydsouza.gsheet.api.GsheetsApi;
import com.sunnydsouza.gsheet.database.DatabaseConnection;

import com.sunnydsouza.gsheet.utils.PropertyFileReader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Imports data from a Google Sheet into a database.
 * @author: sunnydsouza
 */
public class GSheetImporter {

    static String JDBC_CONN_STR = "jdbc:mariadb://192.168.0.109/expenses";
    static String GSHEETS_ID = "1A0SQ8zGTh_QY2FOh8ck-jRpZkra2vN-9F-g-xAmP2hA";

    static Logger logger = LoggerFactory.getLogger(GsheetsApi.class);

    /**
     * Reads configuration from a properties file.
     * Please refer the README.md to understand how to configure the properties file.
     * @param propertyFiles
     */
    public static void readConfiguration(String... propertyFiles) {
        PropertyFileReader.readPropertyFiles(propertyFiles);
        JDBC_CONN_STR = PropertyFileReader.getPropValues("JDBC_CONN_STR");
        GSHEETS_ID = PropertyFileReader.getPropValues("GSHEETS_ID");
        DatabaseConnection.getDatabaseConnection(JDBC_CONN_STR, PropertyFileReader.getPropValues("username"), PropertyFileReader.getPropValues("password"));
    }


    /**
     * Imports data from a Google Sheet (sheet and range) into a database.
     * @param dbTableName This is the name of the table in the database AS WELL AS the name of the sheet in the google sheet.Case sensitive.
     * @param googleSheetRange  The range of the sheet in the google sheet.
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws SQLException
     */
    public static void importGsheet(String dbTableName, String googleSheetRange) throws GeneralSecurityException, IOException, SQLException {
        importGsheet(dbTableName, googleSheetRange, null);
    }

    /**
     * Imports data from a Google Sheet (sheet and range) into a database.
     * @param dbTableName This is the name of the table in the database AS WELL AS the name of the sheet in the google sheet.Case sensitive.
     * @param googleSheetRange The range of the sheet in the google sheet.
     * @param tableTransformer This is a custom transformer to transform data (from columns) in sheets BEFORE importing to database.
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws SQLException
     */
    public static void importGsheet(String dbTableName, String googleSheetRange, TableTransformer tableTransformer) throws GeneralSecurityException, IOException, SQLException {
        logger.debug("tableName:{}", dbTableName);

        DatabaseConnection.executeDmlDdlQuery(DatabaseConnection.getCurrentConnection(JDBC_CONN_STR).prepareStatement("TRUNCATE TABLE " + dbTableName));
        List<Map<String, String>> tableDataMap = GsheetsApi.readSheetValuesAsListMap(GSHEETS_ID, googleSheetRange);

        if(tableTransformer != null) {
            tableDataMap = tableTransformer.transform(tableDataMap);
        }

        for (Map<String, String> row : tableDataMap) {
            PreparedStatement stmt = prepareSqlStmtForRow(dbTableName, row);
            DatabaseConnection.executeDmlDdlQuery(stmt);
        }
    }

    /**
     * Helper method to remove trailing comma for dynamically prepared sql statements.
     * @param s
     * @return
     */
    static String removeTrailingComma(String s) {
        return StringUtils.stripEnd(s, ",");
    }


    /**
     * Prepares a SQL statement for a row of data.
     * @param tableName
     * @param eachRowMap
     * @return PreparedStatement
     * @throws SQLException
     */
    static PreparedStatement prepareSqlStmtForRow(String tableName, Map<String, String> eachRowMap) throws SQLException {
        PreparedStatement stmt;
        String valuesSqlString = "";
        String finalSqlString = "";
        String colsSqlString = "";
        List<String> keysList = new LinkedList<>(eachRowMap.keySet());
        List<String> valuesList = new LinkedList<>(eachRowMap.values());

        for (String eachCol : keysList) {
            colsSqlString = colsSqlString + eachCol + ",";
            valuesSqlString = valuesSqlString + "?,";
        }

        colsSqlString = removeTrailingComma(colsSqlString);
        valuesSqlString = removeTrailingComma(valuesSqlString);


        finalSqlString = "INSERT INTO " + tableName + "(" + colsSqlString + ") values (" + valuesSqlString + ")";

        stmt = DatabaseConnection.getCurrentConnection(JDBC_CONN_STR).prepareStatement(finalSqlString);


        for (int i = 0; i < valuesList.size(); i++) {
            stmt.setString(i + 1, valuesList.get(i).trim().equals("") ? null : valuesList.get(i).trim());
        }


        return stmt;
    }
}
