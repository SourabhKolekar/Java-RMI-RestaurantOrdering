package Operations;

import Operations.Client.Utility.DBConnect;
import Utility.ReadCSV;
import pojo.menuDetails;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static Operations.Client.Utility.DBConnect.*;
import static Utility.commonConstants.*;

/*
Developing the Implementation Class
In the Implementation class (Remote Object) of this application, we are trying to create a window which displays GUI content, using JavaFX.
*/

// Implementing the remote interface RestaurantOrderRemoteInterface that contains void animation() throws RemoteException;
public class RestaurantOrderImpl implements RestaurantOrderRemoteInterface {

//    @Override
//    public void animation() throws RemoteException {
//
//    }


    @Override
    public void animation() throws RemoteException {
        System.out.println("default animation invoked");
    }

    @Override
    public Map<String, ArrayList<menuDetails>> loadDropDown(String mealType) {
        ReadCSV read = new ReadCSV();
        return getConsumables(read.csvQueryDropDownList(), mealType);
    }

    /**
     * returns a map containing separated food and beverage arrayList from total foodList
     *
     * @param foodList total foodList received from server
     * @param mealType mealType selected by user
     * @return a map containing separated food and beverage arrayList
     */
    private Map<String, ArrayList<menuDetails>> getConsumables(ArrayList<menuDetails> foodList, String mealType) {
        // using streams and lambda functionality to filter data on mealType and MenuDescription
        ArrayList<menuDetails> foodItemsList = foodList.stream()
                .filter(n -> n.getMenuDesc().equalsIgnoreCase(FOOD) && n.getMealType().equalsIgnoreCase(mealType))
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<menuDetails> beverageItemsList = foodList.stream()
                .filter(n -> n.getMenuDesc().equalsIgnoreCase(BEVERAGE) && n.getMealType().equalsIgnoreCase(mealType))
                .collect(Collectors.toCollection(ArrayList::new));

        Map<String, ArrayList<menuDetails>> tempMap = new HashMap<>();
        tempMap.put(FOOD_LIST, foodItemsList);
        tempMap.put(BEVERAGE_LIST, beverageItemsList);
        return tempMap;
    }


    public ArrayList<String> dbOrderTableData(int orderID) {
        ArrayList<String> odrList = new ArrayList<>();
        try {
            Class.forName(JDBC_DRIVER);
            Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from Orders where OrderID=" + orderID);
            while (rs.next()) {
                odrList.add(rs.getString(2));
                odrList.add(rs.getString(3));
            }
            con.close();
        } catch (Exception e) {
            System.out.println("db exception:" + e.toString());
        }
        return odrList;
    }

    /**
     * function to enter data to database
     *
     * @param toogleGroupValue     selected meal type
     * @param customerName         name of customer
     * @param custTableNumber      table number of customer
     * @param selectedFoodItem     selected food item
     * @param selectedBeverageItem selected beverage item
     */
    public void insertOrderDataToDB(String toogleGroupValue, String customerName, String custTableNumber, String selectedFoodItem, String selectedBeverageItem) {
        String insertStmt = "INSERT INTO customerDetails(cusName, cusTable,MealType)" +
                "VALUES('" + customerName + "'," + Integer.parseInt(custTableNumber) + ",'" + toogleGroupValue + "');";
        DBConnect db = new Operations.Client.Utility.DBConnect();
        int OrderID = db.dbconnectExecute(insertStmt);
        insertStmt = "INSERT INTO Orders(OrderID,FoodName,BeverageName,orderStatus)" +
                "VALUES('" + OrderID + "','" + selectedFoodItem + "','" + selectedBeverageItem + "',0);";
        db.dbconnectExecute(insertStmt);
    }

    public void initialDBUpload() throws SQLException {
        String line;
        String cvsSplitBy = ",";
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        try (BufferedReader br = new BufferedReader(new FileReader(SRC_OPERATIONS_DATA_CSV_FILE_PATH))) {

            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] food = line.split(cvsSplitBy);
                System.out.println("food:" + Arrays.toString(food));

                DBConnect dbConnect = new DBConnect();

                String insertTableSQL = "INSERT INTO menu VALUES"
                        + "(?,?,?,?,?,?,?,?,?,?)";

                dbConnection = dbConnect.getConnection();
                preparedStatement = dbConnection.prepareStatement(insertTableSQL);
                preparedStatement.setString(1, food[0]);
                preparedStatement.setString(2, food[1]);
                preparedStatement.setString(3, food[2]);
                preparedStatement.setString(4, food[3]);
                preparedStatement.setString(5, food[4]);
                preparedStatement.setString(6, food[5]);
                preparedStatement.setString(7, food[6]);
                preparedStatement.setString(8, food[7]);
                preparedStatement.setString(9, food[8]);
                preparedStatement.setString(10, food[9]);

                // execute insert SQL stetement
                preparedStatement.executeUpdate();
                System.out.println("Record is inserted into DBUSER table!");

            }
        } catch (IOException e) {
            System.out.println("exception:" + e);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }
        }

    }

    public void clearMenuDb() {
        String statement = "truncate table menu";
        DBConnect db = new Operations.Client.Utility.DBConnect();
        db.dbconnectExecute(statement);
        System.out.println("db truncated");
    }
}
