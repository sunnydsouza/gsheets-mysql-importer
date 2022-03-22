package com.sunnydsouza.gsheet.api;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Basic wrapper over Google Sheets API, based on examples from https://developers.google.com/sheets/api/quickstart/java
 * Allows for basic CRUD operations on Google Sheets
 * @author sunnydsouza
 */
public class GsheetsApi {
  private static final String APPLICATION_NAME = "Simple Gsheets API wrapper by sunnydsouza";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String TOKENS_DIRECTORY_PATH = "tokens";
  static final Logger logger= LoggerFactory.getLogger(GsheetsApi.class);

  /**
   * Global instance of the scopes required by this quickstart. If modifying these scopes, delete
   * your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

  private static final String CREDENTIALS_FILE_PATH =
      System.getProperty("user.dir") + "/credentials/credentials.json.backup";

  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json.backup file cannot be found.
   */
  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
      throws IOException {
    // Load client secrets.
    // InputStream in = GsheetsApi.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }

  /**
   * Gets the headers of the table for a given sheet range
   *
   * @param spreadsheetId The spreadsheet id
   * @param range The range of the sheet
   * @return result rows in form of list of list of objects
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public static List<List<Object>> getTableHeaders(String spreadsheetId, String range)
      throws GeneralSecurityException, IOException {

    return readSheetValues(spreadsheetId, range);
  }

    /**
     * Gets the values of a Google sheet for a given sheet range
     * @param spreadsheetId The spreadsheet id
     * @param range The range of the sheet
     * @return result rows in form of list of list of objects
     * @throws GeneralSecurityException
     * @throws IOException
     */
  public static List<List<Object>> readSheetValues(String spreadsheetId, String range)
      throws GeneralSecurityException, IOException {

    // Build a new authorized API client service.
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

    Sheets service =
        new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();

    ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
    List<List<Object>> values = response.getValues();
    if (values == null || values.isEmpty()) {
      return null;
    } else {
      return values;
    }
  }

    /**
     * Appends rows to a Google sheet AFTER a given sheet range
     * @param spreadsheetId The spreadsheet id
     * @param range      The range of the sheet
     * @param rowValues The rows to be appended in form of List<Object>
     * @return updated row count
     * @throws GeneralSecurityException
     * @throws IOException
     */
  public static Integer appendSheetValues(
      String spreadsheetId, String range, List<Object> rowValues)
      throws GeneralSecurityException, IOException {

    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

    Sheets service =
        new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();

    List<List<Object>> values =
        Arrays.asList(
            rowValues // Rows
            );

    ValueRange body = new ValueRange().setValues(values);
    AppendValuesResponse result =
        service
            .spreadsheets()
            .values()
            .append(spreadsheetId, range, body)
            .setValueInputOption("USER_ENTERED")
            .execute();
    System.out.println(result.getUpdates().getUpdatedCells());
    return result.getUpdates().getUpdatedCells();
  }

  // Deletes rows that matching the date codition in the first column of the sheet. SPECIFIC USE
  // CASE ONLY
  public static void deleteRowMatchingDate(
      String spreadsheetId, String sheetRange, int sheetId, String date)
      throws GeneralSecurityException, IOException {
    List<List<Object>> g = GsheetsApi.readSheetValues(spreadsheetId, sheetRange);
    boolean foundRow = false;
    int startRowIndex = 0;
    int rowNo = 0;
    int consecutiveRows = 0;

    //        String searchString = "11/08/2021";
    for (List<Object> row : g) {

      if (row.get(0).equals(date)) {
        if (foundRow == false) startRowIndex = rowNo;
        foundRow = true;

        System.out.println(foundRow);
      } else foundRow = false;
      if (foundRow) consecutiveRows++;
      rowNo++;
    }

    System.out.println(startRowIndex);
    System.out.println(consecutiveRows);
    for (int k = 0; k < consecutiveRows; k++) {

      deleteRow(spreadsheetId, sheetId, startRowIndex, startRowIndex + 1);
    }
  }

    /**
     * Deletes a row in a Google sheet based on start and end row index
     * @param spreadsheetId The spreadsheet id
     * @param sheetId   The sheet id in the spreadsheet
     * @param startIndex    The start row index
     * @param endIndex    The end row index
     * @throws GeneralSecurityException
     * @throws IOException
     */
  public static void deleteRow(String spreadsheetId, int sheetId, int startIndex, int endIndex)
      throws GeneralSecurityException, IOException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Sheets service =
        new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();

    BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest();

    DeleteDimensionRequest request =
        new DeleteDimensionRequest()
            .setRange(
                new DimensionRange()
                    .setSheetId(sheetId) // Sheet ID
                    .setDimension("ROWS")
                    .setStartIndex(startIndex)
                    .setEndIndex(endIndex));

    List<Request> requests = new ArrayList<>();
    requests.add(new Request().setDeleteDimension(request));
    content.setRequests(requests);
    System.out.println(content.getRequests());

    try {
      service.spreadsheets().batchUpdate(spreadsheetId, content).execute();
    } catch (IOException e) {
      e.printStackTrace();
        logger.error("Error in deleting row");
    }
  }

    /**
     * Reads the values of a sheet in a Google sheet and return as List<Map<String, String>>
     * @param spreadsheetId The spreadsheet id
     * @param sheetRange The sheet range
     * @return list of result rows
     * @throws GeneralSecurityException
     * @throws IOException
     */
  public static List<Map<String, String>> readSheetValuesAsListMap(
      String spreadsheetId, String sheetRange) throws GeneralSecurityException, IOException {
    List<List<Object>> sheetValues = GsheetsApi.readSheetValues(spreadsheetId, sheetRange);
    List<Object> tableHeader = getTableHeader(sheetValues);
    List<List<Object>> tableData = getTableData(sheetValues);

    List<Map<String, String>> tableDataMap =
        tableData.stream().map(l -> createColumnMap(tableHeader, l)).collect(toList());

    return tableDataMap;
  }

    /**
     * Helper method to create a map of column name and value using the table header and data. Used in {@link GsheetsApi#readSheetValuesAsListMap(String, String)}
     * @param tableHeader
     * @param row
     * @return
     */
  private static Map<String, String> createColumnMap(List<Object> tableHeader, List<Object> row) {
    Map<String, String> colMap = new HashMap<>();
    int i = 0;
    for (Object eachColHeader : tableHeader) {
      if (i < row.size()) colMap.put((String) eachColHeader, (String) row.get(i));
      i++;
    }
    return colMap;
  }

  /**
   * Helps find rows matching a set of Predicate conditions Assumes that the first row of the
   * sheetRange is a header row
   *
   * @param spreadsheetId
   * @param sheetRange
   * @param conditions
   * @throws GeneralSecurityException
   * @throws IOException
   * @return
   */
  public static List<Integer> findRows(
      String spreadsheetId, String sheetRange, GColumnFilters conditions)
      throws GeneralSecurityException, IOException {
    List<Map<String, String>> tableDataMap = GsheetsApi.readSheetValuesAsListMap(spreadsheetId, sheetRange);

    List<Integer> filteredRowNos =
            IntStream.range(0, tableDataMap.size())
                    .filter(i ->
                            (conditions.apply().test(tableDataMap.get(i))))
                    .mapToObj(i -> i + 2) // +2 because the first row is the header row and index starts from 0
                    .collect(toList());

    return filteredRowNos;
  }

  public static List<Map<String, String>> filterRows(
          String spreadsheetId, String sheetRange, GColumnFilters conditions)
          throws GeneralSecurityException, IOException {
    List<Map<String, String>> tableDataMap = GsheetsApi.readSheetValuesAsListMap(spreadsheetId, sheetRange);
    return tableDataMap.stream()
            .filter(conditions.apply())
            .collect(toList());
  }


  private static List<List<Object>> getTableData(List<List<Object>> sheetValues) {
    return sheetValues.subList(1, sheetValues.size());
  }

  private static List<Object> getTableHeader(List<List<Object>> sheetValues) {
    return sheetValues.get(0);
  }



}
