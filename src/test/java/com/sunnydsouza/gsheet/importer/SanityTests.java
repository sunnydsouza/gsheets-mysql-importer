package com.sunnydsouza.gsheet.importer;
/*
 * @created 21/03/2022 - 4:15 PM
 * @author sunnydsouza
 */

import com.sunnydsouza.gsheet.TableTransformer;
import com.sunnydsouza.gsheet.api.GColumnFilters;
import com.sunnydsouza.gsheet.api.GCondition;
import com.sunnydsouza.gsheet.api.GsheetsApi;
import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SanityTests {

//  static final String PROPERTY_FILE = "configuration/expenses.properties";    //for LOCAL testing
  static final String PROPERTY_FILE = "configuration/sampletest.properties";  //for GITHUB_ACTIONS

  @Test
  public void filterRowSingleColumnGSheets() throws GeneralSecurityException, IOException {
    GSheetImporter.readConfiguration(PROPERTY_FILE);
    List<Map<String, String>> rows =
        GsheetsApi.spreadsheet(GSheetImporter.GSHEETS_ID).filterRows(
            "Filters!A:F",
            GColumnFilters.onCol("RecordedTimestamp")
                .conditions(GCondition.equals("01/03/2022").or(GCondition.equals("11/04/2022"))));
    System.out.println(rows);
  }

  @Test
  public void filterRowMultipleColumnsGSheets() throws GeneralSecurityException, IOException {
    GSheetImporter.readConfiguration(PROPERTY_FILE);
    List<Map<String, String>> rows =
        GsheetsApi.spreadsheet(GSheetImporter.GSHEETS_ID).filterRows(
            "Filters!A:F",
            GColumnFilters.onCol("RecordedTimestamp")
                .conditions(GCondition.equals("01/03/2022").or(GCondition.equals("11/04/2022")))
                .onCol("ExpenseCategory")
                .conditions(GCondition.equals("Investments")));
    System.out.println(rows);
  }

  @Test
  public void filteredRowNoSingleColumnGSheets() throws GeneralSecurityException, IOException {
    GSheetImporter.readConfiguration(PROPERTY_FILE);
    List<Integer> rowNos =
        GsheetsApi.spreadsheet(GSheetImporter.GSHEETS_ID).findRows(
            "Filters!A:F",
            GColumnFilters.onCol("RecordedTimestamp")
                .conditions(GCondition.equals("01/03/2022").or(GCondition.equals("11/04/2022"))));
    System.out.println(rowNos);
  }

  @Test
  public void filteredRowNoMultipleColumnsGSheets() throws GeneralSecurityException, IOException {
    GSheetImporter.readConfiguration(PROPERTY_FILE);
    List<Integer> rowNos =
        GsheetsApi.spreadsheet(GSheetImporter.GSHEETS_ID).findRows(
            "Filters!A:F",
            GColumnFilters.onCol("RecordedTimestamp")
                .conditions(GCondition.equals("01/03/2022").or(GCondition.equals("11/04/2022")))
                .onCol("ExpenseCategory")
                .conditions(GCondition.equals("Investments")));
    System.out.println(rowNos);
  }

  @Test
  public void filterRowsDatesGreaterThan() throws GeneralSecurityException, IOException {
    GSheetImporter.readConfiguration(PROPERTY_FILE);
    List<Integer> rowNos =
        GsheetsApi.spreadsheet(GSheetImporter.GSHEETS_ID).findRows(
            "Filters!A:F",
            GColumnFilters.onCol("RecordedTimestamp")
                .conditions(GCondition.datesGreaterThan("2022-03-01 00:00:00.000", "dd/MM/yyyy")));
    System.out.println(rowNos);
  }

  @Test
  public void filterRowsDatesGreaterThanOrEquals() throws GeneralSecurityException, IOException {
    GSheetImporter.readConfiguration(PROPERTY_FILE);
    List<Integer> rowNos =
            GsheetsApi.spreadsheet(GSheetImporter.GSHEETS_ID).findRows(
                    "Filters!A:F",
                    GColumnFilters.onCol("RecordedTimestamp")
                            .conditions(GCondition.datesGreaterThanOrEquals("2022-03-01 00:00:00.000", "dd/MM/yyyy")));
    System.out.println(rowNos);
  }

  @Test
  public void filterRowsDatesBetween() throws GeneralSecurityException, IOException {
    GSheetImporter.readConfiguration(PROPERTY_FILE);
    List<Integer> rowNos =
            GsheetsApi.spreadsheet(GSheetImporter.GSHEETS_ID).findRows(
                    "Filters!A:F",
                    GColumnFilters.onCol("RecordedTimestamp")
                            .conditions(GCondition.datesBetween("2022-01-01 00:00:00.000", "2022-02-28 00:00:00.000","dd/MM/yyyy")));
    System.out.println(rowNos);
  }

  @Test
  public void importGoogleSheetDb() throws GeneralSecurityException, IOException, SQLException {
    GSheetImporter.readConfiguration(PROPERTY_FILE);
    GSheetImporter.importGsheet(
        "Expense",
        "Expense!A:F");
  }

  @Test
  public void importGoogleSheetDbWithColTransformer() throws SQLException, GeneralSecurityException, IOException {
    GSheetImporter.readConfiguration(PROPERTY_FILE);
    GSheetImporter.importGsheet(
            "ExpenseDateTransform",
            "ExpenseDateTransform!A:F",
            new TableTransformer()
                    .addColumnTransformer("RecordedTimestamp", new RecordedTimestampTransformer()));
  }
}
