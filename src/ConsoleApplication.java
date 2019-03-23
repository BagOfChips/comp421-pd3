import java.math.BigInteger;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;

public class ConsoleApplication {
	
	/*global to do:
	 * need to make creating a customer default and not creating a person otherwise we wont be able to access the cust
	 * properties (i.e having a cart, purchasing etc...)
	 */
	


  public static void main(String[] args) throws SQLException {
    Connection connection = null;
    Statement statement = null;
    try {
      connection = initializeConnection();
      statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

      startApplication(statement);
      // we now have the user's email - Page.userEmail



      // todo: create THE MAIN PAGE - this page should persist, so we dont have to recreate it when going back to it
      //main page should: ask the user for commands i.e: either display a category, display a product, view the cart, 
      //view purchase history, view purchase cart page
      String sqlSelect="";
      ResultSet rs = null;
      
      boolean quit = false;
      while(!quit){
    	  
    	  int userInput= 0;
    	 
		  //persisting main page and asking user for options:
		  userInput = displayMainPage(statement);
    	  
    	  switch(userInput){
    	  
    	  	case 0:
    	  		sqlSelect = "SELECT product_name FROM product WHERE category = 'tablet';";
    	  		log("\n\nHere are all the tablets at Scamazon.\n\n");
    	  		
    	  		rs = DBQueryRunner.getSelection(sqlSelect, statement);
    	  		String selectedProduct = displayAndSelectProduct(rs, statement);
    	  		displayBuyProduct(selectedProduct, statement);
    	  		break;
    	  	case 1:
    	  		sqlSelect = "SELECT product_name FROM product WHERE category = 'laptop';";
    	  		log("\n\nHere are all the laptops at Scamazon.\n\n");
    	  		
    	  		rs = DBQueryRunner.getSelection(sqlSelect, statement);
    	  		String selectedProduct2 =displayAndSelectProduct(rs, statement);
    	  		displayBuyProduct(selectedProduct2, statement);
    	  		break;
    	  	case 2:
    	  		sqlSelect = "SELECT product_name FROM product WHERE category = 'desktop';";
    	  		log("\n\nHere are all the desktops at Scamazon.\n\n");
    	  		
    	  		rs = DBQueryRunner.getSelection(sqlSelect, statement);
    	  		String selectedProduct3 =displayAndSelectProduct(rs, statement);
    	  		displayBuyProduct(selectedProduct3, statement);
    	  		break;
    	  	case 3:
    	  		sqlSelect = 
    	  		"SELECT pr.product_name, pu.date FROM purchases pu"
    	  		+ " JOIN updates u ON pu.cart_id=u.cart_id"
    	  		+ " JOIN cartitem c ON c.cartitem_id=u.cartitem_id"
    	  		+ " JOIN product pr ON pr.product_id=c.product_id"
    	  		+ " WHERE pu.email='" + Page.getUserEmail() + "';";
    	  		
    	  		
    	  		rs = DBQueryRunner.getSelection(sqlSelect, statement);
    	  		log("\n\nHere is the purchase history for " + Page.getUserEmail() + ":\n\n");
    	  		DBQueryRunner.logResultSet(rs);
    	  		break;
    	  	case 4:
    	  		quit=true;
    	  		log("Quitting app and loging out.");
    	  		break;
			  	// todo: create purchase
    	  
    	  		//case if cust wants to go back to main page
    	  }
      }


    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (statement != null) statement.close();
      if (connection != null) connection.close();
      Page.SCANNER.close();
    }
  }
  
  private static void displayBuyProduct(String selectedProduct, Statement statement){
	  String header = "Do you want to buy the product?";
	  String[] options = {"Yes", "No"};
	  
	  Page buyProduct = new Page(header, options);
	  
	  int selection = buyProduct.displayPageWrapper();
	  
	  if (selection == 1) return;
	  
	  log("\n\nBuying product:\n\n");
	  
	  ResultSet carts = DBQueryRunner.getSelection("SELECT * FROM cart;", statement);
	  int numCarts=0;
	  try {
		carts.last();
		numCarts = carts.getRow();
	
	  
		  String sqlCreateCart = "INSERT INTO cart VALUES ("+(numCarts + 1) +");";
		  DBQueryRunner.runQuery(sqlCreateCart, statement);
		  
		  //fetching product id
		  String sqlProdId = "SELECT product_id FROM product WHERE product_name='" + selectedProduct + "';";
		  ResultSet pid = DBQueryRunner.getSelection(sqlProdId, statement);
		  pid.next();
		  String prodId= pid.getString(1);
		  
		  ResultSet cartitems = DBQueryRunner.getSelection("SELECT * FROM cartitem;", statement);
		  cartitems.last();
		  int numCartItems = cartitems.getRow();
		  String sqlCreateCartitem = "INSERT INTO cartitem VALUES ("+(numCartItems+1) + ", " + prodId +", 1);";
		  DBQueryRunner.runQuery(sqlCreateCartitem, statement);
		  
		  String sqlUpdate = "INSERT INTO updates VALUES ("+(numCarts+1) + ", "+(numCartItems+1) + ", '" + Page.getUserEmail() + "');";
		  DBQueryRunner.runQuery(sqlUpdate, statement);
		  
		  String sqlQueryPayment = "SELECT card_number FROM payment_info purchases WHERE email='" + Page.getUserEmail() + "';";
		  ResultSet payment = DBQueryRunner.getSelection(sqlQueryPayment, statement);
		  payment.next();
		  String paymentInfo = payment.getString(1);
		  
		  DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		  Date date = new Date(); 
		  String sqlPurchase = "INSERT INTO purchases VALUES("+ (numCarts+1) + ", " + paymentInfo + ", '" + Page.getUserEmail() + "', "+"DATE '"+  dateFormat.format(date) + "');";
		  DBQueryRunner.runQuery(sqlPurchase, statement);
		  
		  
		  
	  } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	  }
  }
  
  private static String displayAndSelectProduct(ResultSet rs,Statement statement) {
	  try {
		String header = "Which product would you like to see?";
		ArrayList<String> productNames = new ArrayList<>();
		
		while (rs.next()) {
			productNames.add(rs.getString(1));
		}
		
		String[] options = new String[productNames.size()];
		Page selectProduct = new Page(header, productNames.toArray(options));
	
		int index = selectProduct.displayPageWrapper();
		
		String selectedProductName = productNames.get(index);
		String sqlQuery = "SELECT product_name, price, category, cname FROM product WHERE product_name='" + selectedProductName + "';";
		ResultSet rs2 = DBQueryRunner.getSelection(sqlQuery, statement);
		
		log("\n\nThis is your selected product\n\n");
		DBQueryRunner.logResultSet(rs2);
		return selectedProductName;
		
		
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  return null;
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
    
    boolean result = DBQueryRunner.runQuery(insertQuery, statement);
    
  //inserting dummy payment info
    String insertPaymentInfo = "INSERT INTO payment_info VALUES (1234567890, '" + email + "', 'VISA');";
    DBQueryRunner.runQuery(insertPaymentInfo, statement);
    
    return result;
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
  
  private static int displayMainPage(Statement statement){
	  final String MAIN_PAGE_HEADER = "What would you like to do?";
	  final String[] MAIN_OPTIONS = {"Display tablets", "Display laptops", "Display desktops", 
			 "View purchase history", "Quit"};
	  Page mainPage = new Page(MAIN_PAGE_HEADER, MAIN_OPTIONS);
	  
	  int result = mainPage.displayPageWrapper();
	  
	  return result;
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