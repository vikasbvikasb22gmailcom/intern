CREATE DATABASE IF NOT EXISTS food_delivery_db;
USE food_delivery_db;

CREATE TABLE Customer (
    CustomerID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100),
    Phone VARCHAR(15),
    Address VARCHAR(255)
);
SELECT * FROM Customer;

CREATE TABLE Restaurant (
    RestaurantID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100),
    Location VARCHAR(100)
);
SELECT * FROM Restaurant;

CREATE TABLE Food_Item (
    FoodID INT PRIMARY KEY AUTO_INCREMENT,
    FoodName VARCHAR(100),
    Price DECIMAL(8,2),
    RestaurantID INT,
    FOREIGN KEY (RestaurantID) REFERENCES Restaurant(RestaurantID)
);

CREATE TABLE Delivery_Person (
    DeliveryID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100),
    Phone VARCHAR(15)
);

CREATE TABLE `Order` (
    OrderID INT PRIMARY KEY AUTO_INCREMENT,
    OrderDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    CustomerID INT,
    DeliveryID INT,
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID),
    FOREIGN KEY (DeliveryID) REFERENCES Delivery_Person(DeliveryID)
);

CREATE TABLE Order_Item (
    OrderID INT,
    FoodID INT,
    Quantity INT,
    PRIMARY KEY (OrderID, FoodID),
    FOREIGN KEY (OrderID) REFERENCES `Order`(OrderID),
    FOREIGN KEY (FoodID) REFERENCES Food_Item(FoodID)
);
DESCRIBE Customer;
SELECT * FROM Delivery_Person;
INSERT INTO Delivery_Person (Name, Phone)
VALUES ('Ramesh', '9876543210');

