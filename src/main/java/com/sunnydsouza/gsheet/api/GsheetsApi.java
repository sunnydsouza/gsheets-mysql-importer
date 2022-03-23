package com.sunnydsouza.gsheet.api;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Basic wrapper over Google Sheets API, based on examples from
 * https://developers.google.com/sheets/api/quickstart/java Allows for basic CRUD operations on
 * Google Sheets
 *
 * @author sunnydsouza
 */
public class GsheetsApi {
  private static final String APPLICATION_NAME = "Simple Gsheets API wrapper by sunnydsouza";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String TOKENS_DIRECTORY_PATH = "tokens";
  /**
   * Global instance of the scopes required by this quickstart. If modifying these scopes, delete
   * your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

  private static final String CREDENTIALS_FILE_PATH =
      System.getProperty("user.dir") + "/credentials/credentials.json";
  final Logger logger = LoggerFactory.getLogger(GsheetsApi.class);
  Sheets service;
  String spreadsheetId;

  private GsheetsApi(String gsheetsId) throws GeneralSecurityException, IOException {
    this.spreadsheetId = gsheetsId;
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    GoogleCredentials googleCredentials;
    try (InputStream credentialsStream = new FileInputStream(CREDENTIALS_FILE_PATH)) {
      googleCredentials = GoogleCredentials.fromStream(credentialsStream).createScoped(SCOPES);
    }
    service =
        new Sheets.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(googleCredentials))
            .setApplicationName(APPLICATION_NAME)
            .build();
  }

  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  /*  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
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
  }*/

  /**
   * Build and return an authorized Sheets API client service.
   *
   * @return
   * @throws IOException
   */
  /*  private static Credential getServiceAccountCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {


  // Initializing the service:

      GoogleCredentials googleCredentials;
      try(InputStream credentialsStream = new FileInputStream(CREDENTIALS_FILE_PATH)) {
        googleCredentials = GoogleCredentials.fromStream(credentialsStream).createScoped(SCOPES);
      }
      service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(googleCredentials))
              .setApplicationName(APPLICATION_NAME)
              .build();
    }*/
  public static GsheetsApi spreadsheet(String gsheetsId)
      throws GeneralSecurityException, IOException {

    return new GsheetsApi(gsheetsId);
  }

  /**
   * Gets the values of a Google sheet for a given sheet range
   *
   * @param range The range of the sheet
   * @return result rows in form of list of list of objects
   * @throws IOException
   */
  public List<List<Object>> readSheetValues(String range) throws IOException {

    ValueRange response = service.spreadsheets().values().get(this.spreadsheetId, range).execute();
    List<List<Object>> values = response.getValues();
    if (values == null || values.isEmpty()) {
      return null;
    } else {
      return values;
    }
  }

  /**
   * Reads the values of a sheet in a Google sheet and return as List<Map<String, String>>
   *
   * @param sheetRange The sheet range
   * @return list of result rows
   * @throws IOException
   */
  public List<Map<String, String>> readSheetValuesAsListMap(String sheetRange) throws IOException {
    List<List<Object>> sheetValues = readSheetValues(sheetRange);
    List<Object> tableHeader = getTableHeader(sheetValues);
    List<List<Object>> tableData = getTableData(sheetValues);

    List<Map<String, String>> tableDataMap =
        tableData.stream().map(l -> createColumnMap(tableHeader, l)).collect(toList());

    return tableDataMap;
  }

  /**
   * Helper method to create a map of column name and value using the table header and data. Used in
   * {@link GsheetsApi#readSheetValuesAsListMap(String)}
   *
   * @param tableHeader
   * @param row
   * @return
   */
  private Map<String, String> createColumnMap(List<Object> tableHeader, List<Object> row) {
    Map<String, String> colMap = new HashMap<>();
    int i = 0;
    for (Object eachColHeader : tableHeader) {
      if (i < row.size()) colMap.put((String) eachColHeader, (String) row.get(i));
      i++;
    }
    return colMap;
  }

  /**
   * Appends rows to a Google sheet AFTER a given sheet range
   *
   * @param range The range of the sheet
   * @param rowValues The rows to be appended in form of List<Object>
   * @return updated row count
   * @throws IOException
   */
  public Integer appendSheetValues(String range, List<Object> rowValues) throws IOException {

    List<List<Object>> values =
        Arrays.asList(
            rowValues // Rows
            );

    ValueRange body = new ValueRange().setValues(values);
    AppendValuesResponse result =
        service
            .spreadsheets()
            .values()
            .append(this.spreadsheetId, range, body)
            .setValueInputOption("USER_ENTERED")
            .execute();
    System.out.println(result.getUpdates().getUpdatedCells());
    return result.getUpdates().getUpdatedCells();
  }

  // Deletes rows that matching the date codition in the first column of the sheet. SPECIFIC USE
  // CASE ONLY
  public void deleteRowMatchingDate(
      String spreadsheetId, String sheetRange, int sheetId, String date) throws IOException {
    List<List<Object>> g = readSheetValues(sheetRange);
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
   *
   * @param spreadsheetId The spreadsheet id
   * @param sheetId The sheet id in the spreadsheet
   * @param startIndex The start row index
   * @param endIndex The end row index
   */
  public void deleteRow(String spreadsheetId, int sheetId, int startIndex, int endIndex) {

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
   * Helps find rows matching a set of Predicate conditions Assumes that the first row of the
   * sheetRange is a header row
   *
   * @param sheetRange
   * @param conditions
   * @throws IOException
   * @return
   */
  public List<Integer> findRows(String sheetRange, GColumnFilters conditions) throws IOException {
    List<Map<String, String>> tableDataMap = readSheetValuesAsListMap(sheetRange);

    List<Integer> filteredRowNos =
        IntStream.range(0, tableDataMap.size())
            .filter(i -> (conditions.apply().test(tableDataMap.get(i))))
            .mapToObj(
                i -> i + 2) // +2 because the first row is the header row and index starts from 0
            .collect(toList());

    return filteredRowNos;
  }

  public List<Map<String, String>> filterRows(String sheetRange, GColumnFilters conditions)
      throws IOException {
    List<Map<String, String>> tableDataMap = readSheetValuesAsListMap(sheetRange);
    return tableDataMap.stream().filter(conditions.apply()).collect(toList());
  }

  private List<List<Object>> getTableData(List<List<Object>> sheetValues) {
    return sheetValues.subList(1, sheetValues.size());
  }

  private List<Object> getTableHeader(List<List<Object>> sheetValues) {
    return sheetValues.get(0);
  }
}
