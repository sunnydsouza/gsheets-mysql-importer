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

  @Test
  public void filterRowSingleColumnGSheets() throws GeneralSecurityException, IOException {
    GSheetImporter.readConfiguration("configuration/expenses.properties");
    List<Map<String, String>> rows =
        GsheetsApi.filterRows(
            GSheetImporter.GSHEETS_ID,
            "Expense!A:F",
            GColumnFilters.onCol("RecordedTimestamp")
                .conditions(GCondition.equals("01/03/2022").or(GCondition.equals("11/04/2022"))));
    System.out.println(rows);
  }

  @Test
  public void filterRowMultipleColumnsGSheets() throws GeneralSecurityException, IOException {
    GSheetImporter.readConfiguration("configuration/expenses.properties");
    List<Map<String, String>> rows =
        GsheetsApi.filterRows(
            GSheetImporter.GSHEETS_ID,
            "Expense!A:F",
            GColumnFilters.onCol("RecordedTimestamp")
                .conditions(GCondition.equals("01/03/2022").or(GCondition.equals("11/04/2022")))
                .onCol("ExpenseCategory")
                .conditions(GCondition.equals("Investments")));
    System.out.println(rows);
  }

  @Test
  public void filteredRowNoSingleColumnGSheets() throws GeneralSecurityException, IOException {
    GSheetImporter.readConfiguration("configuration/expenses.properties");
    List<Integer> rowNos =
        GsheetsApi.findRows(
            GSheetImporter.GSHEETS_ID,
            "Expense!A:F",
            GColumnFilters.onCol("RecordedTimestamp")
                .conditions(GCondition.equals("01/03/2022").or(GCondition.equals("11/04/2022"))));
    System.out.println(rowNos);
  }

  @Test
  public void filteredRowNoMultipleColumnsGSheets() throws GeneralSecurityException, IOException {
    GSheetImporter.readConfiguration("configuration/expenses.properties");
    List<Integer> rowNos =
        GsheetsApi.findRows(
            GSheetImporter.GSHEETS_ID,
            "Expense!A:F",
            GColumnFilters.onCol("RecordedTimestamp")
                .conditions(GCondition.equals("01/03/2022").or(GCondition.equals("11/04/2022")))
                .onCol("ExpenseCategory")
                .conditions(GCondition.equals("Investments")));
    System.out.println(rowNos);
  }

  @Test
  public void filteredRowNoGreaterThan() throws GeneralSecurityException, IOException {
    GSheetImporter.readConfiguration("configuration/expenses.properties");
    List<Integer> rowNos =
        GsheetsApi.findRows(
            GSheetImporter.GSHEETS_ID,
            "Expense!A:F",
            GColumnFilters.onCol("RecordedTimestamp")
                .conditions(GCondition.lessThan("01/03/2022")));
    System.out.println(rowNos);
  }

  @Test
  public void importGoogleSheetDb() throws GeneralSecurityException, IOException, SQLException {
    GSheetImporter.readConfiguration("configuration/expenses.properties");
    GSheetImporter.importGsheet(
        "Expense",
        "Expense!A:F");
  }

  @Test
  public void importGoogleSheetDbWithColTransformer() throws SQLException, GeneralSecurityException, IOException {
    GSheetImporter.readConfiguration("configuration/expenses.properties");
    GSheetImporter.importGsheet(
            "Expense",
            "Expense!A:F",
            new TableTransformer()
                    .addColumnTransformer("RecordedTimestamp", new RecordedTimestampTransformer()));
  }
}
