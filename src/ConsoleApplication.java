
import java.sql.*;
import java.util.Scanner;

public class ConsoleApplication {
	

  public static void main(String[] args) throws SQLException {
	
    Connection connection = null;
    Statement statement = null;

    try {
      connection = initializeConnection();
      statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      
      interact(statement);

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (statement != null) statement.close();
      if (connection != null) connection.close();
      log("DATABASE CONNECTION CLOSED");
    }
  }
  
  private static void interact(Statement statement) throws SQLException {
	  Scanner sc = new Scanner(System.in);
	  String input = "";
	  while (true) {
		  log("Please select an action to perform\n"
		  		+ "SELECT:\t Select data from given databases\n"
		  		+ "ADD:\t Add data to a database\n"
		  		+ "UPDATE:\t Update existing data\n"
		  		+ "DELETE:\t Delete existing data\n"
		  		+ "QUIT:\t Exit session");
		  input = sc.next().toUpperCase();
		  
		  switch (input) {
		  case "QUIT":
			  return;
		  case "SELECT":
			  select(statement, sc);
		  case "ADD":
			  add(statement, sc);
		  case "UPDATE":
			  update(statement, sc);
		  case "DELETE":
			  delete(statement, sc);
		  }
	  }
  }
  
  private static void select(Statement statement, Scanner sc) throws SQLException {
	  try {
		  log("Select a table.");
		  String tableName = sc.next();
		  log("Specify columns to query.");
		  String columns = sc.next();
		  log("Select a condition (put 'no' for no condition)");
		  String condition = sc.next().toUpperCase();
	  
		  String querySQL = "SELECT " + columns + " from " + tableName + ";";
		  if (!condition.equals("NO")) {
			  querySQL = querySQL.substring(0, querySQL.length() - 1);
			  querySQL += " WHERE " + condition + ";";
		  } 
		  System.out.println (querySQL);
		  ResultSet rs = statement.executeQuery (querySQL);
		  ResultSetMetaData rsmd = rs.getMetaData();
		  int columnsNumber = rsmd.getColumnCount();
		  
		  while ( rs.next ( ) ) {
			  
			  for (int i = 1; i <= columnsNumber; i++) {
			      if (i > 1) System.out.print(",  ");
			      String columnValue = rs.getString(i);
			      System.out.print(rsmd.getColumnName(i) + ":\t" + columnValue);
			  }
		      System.out.println();
		      System.out.println("---------------------------------------------------------");
		    }
	  } catch (SQLException e) {
			int sqlCode = e.getErrorCode(); // Get SQLCODE
			String sqlState = e.getSQLState(); // Get SQLSTATE
	                
			System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
	    }
	  
	  
  }
  
  private static void add(Statement statement, Scanner sc) {
	  // TODO
  }
  
  private static void update(Statement statement, Scanner sc) {
	  // TODO
  }
  
  private static void delete(Statement statement, Scanner sc) {
	  // TODO
  }

  private static Connection initializeConnection() {
    final String DATABASE_URL = System.getenv("COMP421_URL");
    final String DATABASE_USER = System.getenv("COMP421_USER");
    final String DATABASE_PASS = System.getenv("COMP421_PASS");
    
	Scanner sc = new Scanner(System.in);
	// Verify user
	log("Please enter the password for cs421g30");
	String pass = sc.next();
	
	if (!pass.equals(DATABASE_PASS)) return null;
    
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
