import java.math.BigInteger;
import java.sql.*;
import java.util.Arrays;

public class ConsoleApplication {

  public static void main(String[] args) throws SQLException {
    Connection connection = null;
    Statement statement = null;
    try {
      connection = initializeConnection();
      statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

      startApplication(statement);
      // we now have the user's email - Page.userEmail



      // todo: create THE MAIN PAGE - this page should persist, so we dont have to recreate it when going back to it

      // todo: create SELECT PRODUCT CATEGORY PAGE - should persist too

      // todo: create DISPLAY PRODUCTS PAGE - to keep it simple, we can just display all the products on 1 page

      // todo: create VIEW CART PAGE

      // todo: create PURCHASE HISTORY PAGE

      // todo: create PURCHASE CART PAGE




      // todo: create purchase


    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (statement != null) statement.close();
      if (connection != null) connection.close();
      Page.SCANNER.close();
    }
  }

  /**
   * After this finishes, Page.userEmail is set
   */
  private static void startApplication(Statement statement) throws SQLException {
    int loginPageResult = createStartPage(statement);
    switch (loginPageResult) {
      case 0: // login
        createLoginPage(statement);
        break;
      case 1: // new user
        createNewUserPage(statement);
        break;
    }
  }

  private static void createNewUserPage(Statement statement) {
    final String NEW_USER_HEADER = "Input your information in the following order:\n" +
                                   "<email> <password> <first_name> <last_name> <phone_number> <home_address>";
    final String[] NEW_USER_OPTIONS = {};
    final String NEW_USER_FOOTER = "eg. youremail2000@gmail.com yourpassword2000 john doe 1234567890 1234 street road";
    Page newUserPage = new Page(NEW_USER_HEADER, NEW_USER_OPTIONS, NEW_USER_FOOTER);

    String[] userInfo = newUserPage.tokenizeDisplayPage();
    while (userInfo.length < 2) {
      log("\nPlease add more information");
      userInfo = newUserPage.tokenizeDisplayPage();
    }

    while (!insertNewUser(userInfo, 50 /* VARCHAR LIMIT */, statement)) {
      log("\nInput invalid, try again");
      userInfo = newUserPage.tokenizeDisplayPage();
    }

    Page.setUserEmail(userInfo[0]);
    log("\nThanks for registering, your email is: " + Page.getUserEmail());
  }

  // todo: sanitize each token
  private static boolean insertNewUser(String[] userInfo, int limit, Statement statement) {
    String email = null, password = null, first_name = null, last_name = null, home_address = null;
    BigInteger phone_number = null;

    if (userInfo.length >= 5) {
      try { phone_number = new BigInteger(userInfo[4]); }
      catch (NumberFormatException e) { /* do nothing */ }
      if (userInfo.length >= 6) {
        home_address = "";
        int i;
        for (i = 4; i < userInfo.length - 1; i++)
          home_address += userInfo[i] + " ";
        home_address += userInfo[i];
      }
      userInfo = Arrays.copyOfRange(userInfo, 0, 4);
    }

    switch (userInfo.length) {
      case 4:
        last_name = userInfo[3];
      case 3:
        first_name = userInfo[2];
      case 2:
        email = userInfo[0];
        password = userInfo[1];
    }

    final String PERSONTABLE = "person";
    final String CUSTOMERTABLE = "customer";
    String insertQuery =
        "INSERT INTO " + PERSONTABLE +
        " (email, password, first_name, last_name, phone_number, home_address) VALUES('" +
        email + "', '" + password + "', " + (first_name == null ? "NULL" : "'" + first_name + "'") + ", " +
        (last_name == null ? "NULL" : "'" + last_name + "'") + ", " + (phone_number == null ? "NULL" : phone_number) + ", " +
        (home_address == null ? "NULL" : "'" + home_address + "'") + ");" +
        "INSERT INTO " + CUSTOMERTABLE + " (email) VALUES('" + email + "');";
    return DBQueryRunner.runQuery(insertQuery, statement);
  }

  private static void createLoginPage(Statement statement) throws SQLException {
    final String LOGIN_PAGE_HEADER = "Input your email and password";
    final String[] LOGIN_PAGE_OPTIONS = {};
    final String LOGIN_PAGE_FOOTER = "eg. youremail2000@gmail.com yourpassword2000";
    Page loginPage = new Page(LOGIN_PAGE_HEADER, LOGIN_PAGE_OPTIONS, LOGIN_PAGE_FOOTER);

    String[] userAndPass = loginPage.tokenizeDisplayPage(2);
    while (!validateLogin(userAndPass, statement))
      userAndPass = loginPage.tokenizeDisplayPage(2);
    Page.setUserEmail(userAndPass[0]);
  }

  /**
   * test with: johndoe@gmail.com 1234!
   */
  private static boolean validateLogin(String[] userAndPass, Statement statement) throws SQLException {
    final String USER = userAndPass[0];
    final String PASS = userAndPass[1];
    final String TABLENAME = "person";

    String selectQuery = "SELECT email, person FROM " + TABLENAME +
                         " WHERE email='" + USER + "' AND password='" + PASS + "';";
    ResultSet resultSet = DBQueryRunner.getSelection(selectQuery, statement);
    if (resultSet.next()) {
      log("\nLogin success, Welcome to Scamazon");
      return true;
    }

    log("\nIncorrect login, try again");
    return false;
  }

  private static int createStartPage(Statement statement) {
    final String START_PAGE_HEADER = "Login or Create a new user";
    final String[] START_OPTIONS = { "Login", "Create new user" };
    Page startPage = new Page(START_PAGE_HEADER, START_OPTIONS);

    return startPage.displayPageWrapper();
  }

  private static Connection initializeConnection() {
    final String DATABASE_URL = System.getenv("COMP421_URL");
    final String DATABASE_USER = System.getenv("COMP421_USER");
    final String DATABASE_PASS = System.getenv("COMP421_PASS");

    Connection connection = null;
    try {
      DriverManager.registerDriver(new org.postgresql.Driver());
      connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASS);
    } catch (Exception e) {
      e.printStackTrace();
      log("DB CONNECTION FAILED - EXITING");
      System.exit(0);
    }
    return connection;
  }

  private static void log(String s) { System.out.println(s); }
}