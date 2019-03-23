import java.sql.*;
import java.util.Map;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VisualizationApplication extends Application {

  private static Statement statement = null;

  public static void main(String[] args) throws SQLException {
    Connection connection = null;
    try {
      connection = initializeConnection();
      statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

      // step 1: ask user for 1 measure
      int measureSelection = getMeasureSelection(statement);

      // step 2: ask user for 1 dimension
      int dimensionSelection = getDimensionSelection(statement);

      // step 3: ask user for 1 chart type
      int chartSelection = getChartSelection(statement);

      // step 4: generate chart
      generateChart(measureSelection, dimensionSelection, chartSelection, statement);

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (statement != null) statement.close();
      if (connection != null) connection.close();
      Page.SCANNER.close();
    }
  }

  // measures:    0 - purchases   1 - products
  // dimensions:  0 - time        1 - category      2 - brand
  // chart type:  0 - bar         1 - pie
  public static void generateChart(int measure, int dimension, int chart, Statement statement) throws SQLException {
    String sDimension = "";
    String sMeasure = "";
    String sqlSelectQuery = "";
    String chartType = "";

    switch (measure) {
      case 0:
        sMeasure = "Total_Purchases";
        switch (dimension) {
          case 0:
            sDimension = "Time";
            sqlSelectQuery = "select count(*) as " + sMeasure + ", " +
                             "to_char(date, 'Mon') as " + sDimension +
                             " from purchases group by " + sDimension + ";";
            break;
          case 1:
            sDimension = "Category";
            sqlSelectQuery = "SELECT COUNT(*) as " + sMeasure + ", " +
                    "pr.category as " + sDimension +
                    " FROM purchases p JOIN updates u ON (p.cart_id=u.cart_id) JOIN cartitem c ON (c.cartitem_id=u.cartitem_id) JOIN product pr ON (c.product_id=pr.product_id) " +
                    "GROUP BY " + sDimension + ";";
            break;
          case 2:
            sDimension = "Brand";
            sqlSelectQuery = "SELECT COUNT(*) as " + sMeasure + ", " +
                    "pr.cname as " + sDimension +
                    " FROM purchases p JOIN updates u ON (p.cart_id=u.cart_id) JOIN cartitem c ON (c.cartitem_id=u.cartitem_id) JOIN product pr ON (c.product_id=pr.product_id) " +
                    "GROUP BY " + sDimension + ";";
            break;
        }
        break;
      case 1:
        sMeasure = "Total_Products";
        switch (dimension) {
          case 0:
            sDimension = "Time";
            log("Selection not supported"); // wont support this case
            return;
          case 1:
            sDimension = "Category";
            sqlSelectQuery = "SELECT COUNT(*) as " + sMeasure +
                    ", category as " + sDimension +
                    " FROM product GROUP BY " + sDimension + ";";
            break;
          case 2:
            sDimension = "Brand";
            sqlSelectQuery = "SELECT COUNT(*) as " + sMeasure +
                    ", cname as " + sDimension +
                    " FROM product GROUP BY " + sDimension + ";";
            break;
        }
        break;
    }

    switch (chart) {
      case 0:
        chartType = "bar";
        break;
      case 1:
        chartType = "pie";
        break;
    }

    String[] params = {
            "--Dimension=" + sDimension,
            "--Measure=" + sMeasure,
            "--SQL=" + sqlSelectQuery,
            "--ChartType=" + chartType
    };
    launch(params);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    Parameters parameters = getParameters();
    Map<String, String> namedParameters = parameters.getNamed();
    String sDimension = namedParameters.get("Dimension");
    String sMeasure = namedParameters.get("Measure");
    String sqlSelect = namedParameters.get("SQL");
    String chartType = namedParameters.get("ChartType");

    final String TITLE = sMeasure + " by " + sDimension;
    primaryStage.setTitle(TITLE);
    CategoryAxis xAxis = new CategoryAxis();
    xAxis.setLabel(sDimension);
    NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel(sMeasure);

    if (chartType.equals("bar")) {
      BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
      ResultSet resultSet = DBQueryRunner.getSelection(sqlSelect, statement);
      while (resultSet.next()) {
        Number measureValue = resultSet.getInt(sMeasure);
        String dimensionValue = resultSet.getString(sDimension);
        XYChart.Series dataSeries = new XYChart.Series();
        dataSeries.getData().add(new XYChart.Data(dimensionValue, measureValue));
        barChart.getData().add(dataSeries);
      }
      VBox vBox = new VBox(barChart);
      Scene scene = new Scene(vBox, 600, 300);
      primaryStage.setScene(scene);
      primaryStage.setHeight(600);
      primaryStage.setWidth(1200);
      primaryStage.show();
    } else if (chartType.equals("pie")) {
      ResultSet resultSet = DBQueryRunner.getSelection(sqlSelect, statement);
      ObservableList<PieChart.Data> observableArrayList = FXCollections.observableArrayList();
      while (resultSet.next()) {
        double measureValue = resultSet.getDouble(sMeasure);
        String dimensionValue = resultSet.getString(sDimension);
        observableArrayList.add(new PieChart.Data(dimensionValue, measureValue));
      }
      PieChart pieChart = new PieChart(observableArrayList);
      VBox vBox = new VBox(pieChart);
      Scene scene = new Scene(vBox, 600, 300);
      primaryStage.setScene(scene);
      primaryStage.setHeight(600);
      primaryStage.setWidth(1200);
      primaryStage.show();
    }
  }

  public static int getChartSelection(Statement statement) {
    final String HEADER = "Select a Chart type";
    final String[] OPTIONS = { "Bar", "Pie" };
    Page ChartSelectionPage = new Page(HEADER, OPTIONS);

    return ChartSelectionPage.displayPageWrapper();
  }

  public static int getDimensionSelection(Statement statement) {
    final String HEADER = "Select a Dimension";
    final String[] OPTIONS = { "Time", "Category", "Brand" };
    Page DimensionSelectionPage = new Page(HEADER, OPTIONS);

    return DimensionSelectionPage.displayPageWrapper();
  }

  public static int getMeasureSelection(Statement statement) {
    final String HEADER = "Select a Measure";
    final String[] OPTIONS = { "Total Purchases", "Total Products" };
    Page MeasureSelectionPage = new Page(HEADER, OPTIONS);

    return MeasureSelectionPage.displayPageWrapper();
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
