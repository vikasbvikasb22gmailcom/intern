import java.sql.*;
import java.util.Scanner;

public class App {

    static final String url = "jdbc:mysql://localhost:3306/food_delivery_db";
    static final String user = "root";
    static final String password = "Vikas@123";

    static Connection con;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws Exception {

        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection(url, user, password);

        while (true) {
            System.out.println("\n--- FOOD DELIVERY SYSTEM ---");
            System.out.println("1. Add Customer");
            System.out.println("2. Add Restaurant");
            System.out.println("3. Add Food Item");
            System.out.println("4. Place Order");
            System.out.println("5. View Orders");
            System.out.println("6. Exit");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1: addCustomer(); break;
                case 2: addRestaurant(); break;
                case 3: addFoodItem(); break;
                case 4: placeOrder(); break;
                case 5: viewOrders(); break;
                case 6: 
                    con.close();
                    System.exit(0);
            }
        }
    }

    static void addCustomer() throws Exception {
        System.out.print("Name: ");
        String name = sc.nextLine();
        System.out.print("Phone: ");
        String phone = sc.nextLine();
        System.out.print("Address: ");
        String address = sc.nextLine();

        String sql = "INSERT INTO Customer (Name, Phone, Address) VALUES (?, ?, ?)";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, name);
        pst.setString(2, phone);
        pst.setString(3, address);
        pst.executeUpdate();

        System.out.println("Customer Added!");
    }

    static void addRestaurant() throws Exception {
        System.out.print("Restaurant Name: ");
        String name = sc.nextLine();
        System.out.print("Location: ");
        String location = sc.nextLine();

       String sql = "INSERT INTO Restaurant (Name, Location) VALUES (?, ?)";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1,name);
        pst.setString(2, location);
        pst.executeUpdate();

        System.out.println("Restaurant Added!");
    }

    static void addFoodItem() throws Exception {
        System.out.print("Food Name: ");
        String name = sc.nextLine();
        System.out.print("Price: ");
        double price = sc.nextDouble();
        System.out.print("Restaurant ID: ");
        int rid = sc.nextInt();
        sc.nextLine();

        String sql = "INSERT INTO Food_Item (FoodName, Price, RestaurantID) VALUES (?, ?, ?)";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, name);
        pst.setDouble(2, price);
        pst.setInt(3, rid);
        pst.executeUpdate();

        System.out.println("Food Item Added!");
    }
static void placeOrder() throws Exception {


    Statement stmt = con.createStatement();
    ResultSet rs1 = stmt.executeQuery("SELECT * FROM Delivery_Person");

    System.out.println("\nAvailable Delivery Persons:");
    while (rs1.next()) {
        System.out.println("ID: " + rs1.getInt("DeliveryID") +
                           " Name: " + rs1.getString("Name"));
    }

    
    System.out.print("\nCustomer ID: ");
    int cid = sc.nextInt();

    System.out.print("Delivery Person ID: ");
    int did = sc.nextInt();
    sc.nextLine();

    String orderSql = "INSERT INTO `Order` (CustomerID, DeliveryID) VALUES (?, ?)";
    PreparedStatement pst = con.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
    pst.setInt(1, cid);
    pst.setInt(2, did);
    pst.executeUpdate();

    ResultSet rs = pst.getGeneratedKeys();
    rs.next();
    int orderId = rs.getInt(1);

    while (true) {
        System.out.print("Food ID: ");
        int fid = sc.nextInt();
        System.out.print("Quantity: ");
        int qty = sc.nextInt();

        String itemSql = "INSERT INTO Order_Item VALUES (?, ?, ?)";
        PreparedStatement pst2 = con.prepareStatement(itemSql);
        pst2.setInt(1, orderId);
        pst2.setInt(2, fid);
        pst2.setInt(3, qty);
        pst2.executeUpdate();

        System.out.print("Add more items? (y/n): ");
        char ch = sc.next().charAt(0);
        if (ch == 'n') break;
    }

    System.out.println("Order Placed Successfully!");
}

    static void viewOrders() throws Exception {
        String sql = "SELECT O.OrderID, C.Name, O.OrderDate " +
                "FROM `Order` O JOIN Customer C ON O.CustomerID = C.CustomerID";

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            System.out.println("OrderID: " + rs.getInt("OrderID") +
                    " Customer: " + rs.getString("Name") +
                    " Date: " + rs.getTimestamp("OrderDate"));
        }
    }
}