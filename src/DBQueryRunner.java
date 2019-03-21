import java.sql.*;

public class DBQueryRunner {

  /**
   * Use this for SELECTION queries
   */
  public static ResultSet getSelection(String sqlSelect, Statement statement) {
    ResultSet resultSet = null;
    try {
      log(sqlSelect);
      resultSet = statement.executeQuery(sqlSelect);
    } catch (SQLException e) {
      logSqlException(e);
    }
    return resultSet;
  }

  public static void runQueries(String[] sqlQueries, Statement statement) {
    for (String sqlQuery: sqlQueries)
      runQuery(sqlQuery, statement);
  }

  /**
   * Run SQL query, return true if successful
   */
  public static boolean runQuery(String sqlQuery, Statement statement) {
    try {
      log(sqlQuery);
      statement.executeQuery(sqlQuery);
    } catch (SQLException e) {
      return logSqlException(e);
    }
    return true;
  }

  /**
   * testing only
   */
  public static void logResultSet(ResultSet resultSet) {
    if (resultSet == null) return;
    try {
      ResultSetMetaData RSMD = resultSet.getMetaData();
      final int NUMBER_COLUMNS = RSMD.getColumnCount();
      for (int i = 1; i <= NUMBER_COLUMNS; i++)
        System.out.print(RSMD.getColumnName(i) + "\t\t");

      while (resultSet.next()) {
        log();
        for (int i = 1; i <= NUMBER_COLUMNS; i++)
          System.out.print(resultSet.getString(i) + "\t\t");
      }
      log();
    } catch (SQLException e) {
      logSqlException(e);
    }
  }

  public static boolean logSqlException(SQLException e) {
    boolean ignoreSqlWarnings = ignoreSqlWarnings(e);
    if (!ignoreSqlWarnings) {
      log("ERROR CODE:"); log(e.getErrorCode());
      log("SQL STATE:"); log(e.getSQLState());
    }
    return ignoreSqlWarnings;
  }

  public static boolean ignoreSqlWarnings(SQLException e) {
    switch (e.getSQLState()) {
      // no data
      case "02000":
      case "02001":
        return true;
    }
    return false;
  }

  public static void log() { System.out.println(); }
  public static void log(String s) { System.out.println(s); }
  public static void log(int n) { System.out.println(n); }
}
