import java.sql.*;

public class ConsoleApplication {

  public static void main(String[] args) throws SQLException {
    Connection connection = null;
    Statement statement = null;

    try {
      connection = initializeConnection();
      statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (statement != null) statement.close();
      if (connection != null) connection.close();
    }
  }

  private static Connection initializeConnection() {
    final String DATABASE_URL = System.getenv("COMP421_URL");
    final String DATABASE_USER = System.getenv("COMP421_USER");
    final String DATABASE_PASS = System.getenv("COMP421_PASS");
    Connection connection = null;
    try {
      DriverManager.registerDriver(new org.postgresql.Driver());
      connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASS);
      log("CONNECTION SUCCESSFUL");
    } catch (Exception e) {
      e.printStackTrace();
      log("TERMINATING PROGRAM");
      System.exit(0);
    }
    return connection;
  }

  private static void log(String s) { System.out.println(s); }
}
