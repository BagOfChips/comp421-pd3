import java.util.Scanner;

public class Page {
  private String header;
  public String[] options;
  private String footer;
  private final String defaultFooter = "Please enter a number: ";

  // static members, stop creating a billion new scanners richard
  private static String userEmail;
  public static final Scanner SCANNER = new Scanner(System.in);

  public static void setUserEmail(String userEmail) { Page.userEmail = userEmail; }
  public static String getUserEmail() { return Page.userEmail; }

  // constructors
  public Page(String header, String[] options) {
    this.header = header;
    this.options = options.clone();
  }

  public Page(String header, String[] options, String footer) {
    this(header, options);
    this.footer = footer;
  }

  /**
   * displays the options, user chooses an option
   */
  public String displayPage() {
    log("\n\n" + this.header + "\n");
    for (int i = 0; i < this.options.length; i++)
      log("\t" + i + ") " + this.options[i]);

    if (footer != null && !footer.isEmpty()) {
      log("\n" + footer);
    } else {
      log("\n" + defaultFooter);
    }

    // get user input, return String for "switch case"
    return SCANNER.nextLine();
  }

  /**
   * Calls displayPage(), tokenizes user input
   */
  public String[] tokenizeDisplayPage(int numExpectedTokens) {
    String[] tokens = tokenizeDisplayPage();
    while (tokens.length != numExpectedTokens) {
      log("Expecting " + numExpectedTokens + " values");
      tokens = tokenizeDisplayPage();
    }
    return tokens;
  }

  public String[] tokenizeDisplayPage() {
    String sResult = this.displayPage();
    String[] tokens = sResult.split(" ");
    return tokens;
  }

  /**
   * Calls displayPage(), loops until user inputs an appropriate option
   */
  public int displayPageWrapper() {
    int iResult = -1;
    String sResult = this.displayPage();
    while (iResult < 0 || iResult >= this.options.length) {
      try {
        iResult = Integer.parseInt(sResult);
      } catch (NumberFormatException e) {
        // do nothing
      }

      if (iResult < 0 || iResult >= this.options.length) {
        log("\nPlease enter a number in [0, " + (this.options.length - 1) + "]: ");
        sResult = SCANNER.nextLine();
      }
    }
    return iResult;
  }

  private void log(String s) { System.out.println(s); }
}
