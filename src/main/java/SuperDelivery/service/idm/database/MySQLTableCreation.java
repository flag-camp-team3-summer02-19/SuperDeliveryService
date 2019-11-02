package SuperDelivery.service.idm.database;

import SuperDelivery.service.idm.IDMService;
import SuperDelivery.service.idm.logger.ServiceLogger;

import java.sql.*;

public class MySQLTableCreation {
    // Database update event interval (60 sec)
    private static final int UPDATE_INTERVAL = 60;

    public static void CreateTable(boolean drop, boolean fakeData) {
        try {
            // Step 1 Connect to MySQL.
            Connection conn = IDMService.getCon();

            if (conn == null) {
                ServiceLogger.LOGGER.config("No database connection.");
                return;
            }

            Statement statement = conn.createStatement();
            String sql = null;

            // Setup MySQL server current time zone (PST).
            sql = "SET GLOBAL time_zone = '-7:00'";
            statement.executeUpdate(sql);

            if (drop) {
                // Step 2 Drop tables in case they exist.
                sql = "DROP TABLE IF EXISTS sessions";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS user_address";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS session_status";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS orders";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS  workers";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS users";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS package_info";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS delivery_info";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS location_info";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS delivery_type";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS  delivery_status";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS  warehouse_info";
                statement.executeUpdate(sql);
            }

            // Step 3 Create new tables if they do not exist
            sql = "CREATE TABLE IF NOT EXISTS users ("
                    + "userID int AUTO_INCREMENT,"
                    + "email VARCHAR(50) NOT NULL,"
                    + "salt VARCHAR(8) NOT NULL,"
                    + "pword VARCHAR(128) NOT NULL,"
                    + "UNIQUE (email),"
                    + "PRIMARY KEY (userID)"
                    + ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS session_status ("
                    + "statusID int NOT NULL,"
                    + "status VARCHAR(20) NOT NULL,"
                    + "PRIMARY KEY (statusID)"
                    + ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS sessions ("
                    + "sessionID VARCHAR(128) NOT NULL,"
                    + "email VARCHAR(50) NOT NULL,"
                    + "status int NOT NULL,"
                    + "timeCreated timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                    + "lastUsed timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                    + "exprTime timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                    + "PRIMARY KEY (sessionID),"
                    + "FOREIGN KEY (email) REFERENCES users(email) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (status) REFERENCES session_status(statusID) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS user_address ("
                    + "email VARCHAR(50) NOT NULL,"
                    + "street VARCHAR(128) NOT NULL,"
                    + "city VARCHAR(32) NOT NULL,"
                    + "state VARCHAR(8) NOT NULL,"
                    + "zipcode int NOT NULL,"
                    + "PRIMARY KEY (email),"
                    + "FOREIGN KEY (email) REFERENCES users (email) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS package_info ("
                    + "packageID int AUTO_INCREMENT,"
                    + "pkgLength FLOAT NOT NULL,"
                    + "pkgWidth FLOAT NOT NULL,"
                    + "pkgHeight FLOAT NOT NULL,"
                    + "pkgWeight FLOAT NOT NULL,"
                    + "pkgFrom VARCHAR(255) NOT NULL,"
                    + "pkgTo VARCHAR(255) NOT NULL,"
                    + "pkgNotes VARCHAR(255) NOT NULL,"
                    + "PRIMARY KEY (packageID)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS delivery_type ("
                    + "deliveryTypeID int NOT NULL,"
                    + "deliveryType VARCHAR(20) NOT NULL,"
                    + "PRIMARY KEY (deliveryTypeID)"
                    + ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS delivery_status ("
                    + "deliveryStatusID int NOT NULL,"
                    + "deliveryStatus VARCHAR(20) NOT NULL,"
                    + "PRIMARY KEY (deliveryStatusID)"
                    + ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS delivery_info ("
                    + "deliveryID int AUTO_INCREMENT,"
                    + "deliveryType int NOT NULL,"
                    + "deliveryTime timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                    + "deliveryStatus int NOT NULL,"
                    + "cost FLOAT NOT NULL,"
                    + "PRIMARY KEY (deliveryID),"
                    + "FOREIGN KEY (deliveryType) REFERENCES delivery_type (deliveryTypeID) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (deliveryStatus) REFERENCES delivery_status (deliveryStatusID) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS location_info ("
                    + "locationID int AUTO_INCREMENT,"
                    + "currentLat DECIMAL(9,6) NOT NULL,"
                    + "currentLon DECIMAL(9,6) NOT NULL,"
                    + "destinationLat DECIMAL(9,6) NOT NULL,"
                    + "destinationLon DECIMAL(9,6) NOT NULL,"
                    + "PRIMARY KEY (locationID)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS warehouse_info ("
                    + "warehouseID ENUM('1', '2', '3') NOT NULL,"
                    + "lat DECIMAL(9,6) NOT NULL,"
                    + "lon DECIMAL(9,6) NOT NULL,"
                    + "PRIMARY KEY (warehouseID)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS workers ("
                    + "workerID int AUTO_INCREMENT,"
                    + "workerType ENUM('ROBOT', 'DRONE') NOT NULL,"
                    + "warehouse ENUM('1', '2', '3') NOT NULL,"
                    + "holdFor VARCHAR(50),"
                    + "holdTime TIMESTAMP,"
                    + "pickupTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                    + "availableTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                    + "workerLat DECIMAL(9,6) NOT NULL,"
                    + "workerLon DECIMAL(9,6) NOT NULL,"
                    + "PRIMARY KEY (workerID),"
                    + "FOREIGN KEY (warehouse) REFERENCES warehouse_info(warehouseID)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS orders ("
                    + "orderID int AUTO_INCREMENT,"
                    + "email VARCHAR(50) NOT NULL,"
                    + "orderedTime timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                    + "package int NOT NULL,"
                    + "delivery int NOT NULL,"
                    + "location int NOT NULL,"
                    + "worker int,"
                    + "token VARCHAR(50) NOT NULL,"
                    + "transactionID VARCHAR(50),"
                    + "PRIMARY KEY (orderID),"
                    + "FOREIGN KEY (email) REFERENCES users (email) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (package) REFERENCES package_info (packageID) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (delivery) REFERENCES delivery_info (deliveryID) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (location) REFERENCES location_info (locationID) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (worker) REFERENCES workers (workerID) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")";
            statement.executeUpdate(sql);

            // Step 4 Insert constant data into tables
            sql = "INSERT IGNORE INTO session_status (statusID, status) VALUES (1, 'ACTIVE'), (2, 'CLOSED'), (3, 'EXPIRED'), (4, 'REVOKED')";
            statement.execute(sql);
            sql = "INSERT IGNORE INTO delivery_type (deliveryTypeID, deliveryType) VALUES (3, 'ROBOT'), (4, 'DRONE')";
            statement.execute(sql);
            sql = "INSERT IGNORE INTO delivery_status (deliveryStatusID, deliveryStatus) VALUES (0, 'Order Placed'), (1, 'In Progress'), (2, 'Delivered')";
            statement.execute(sql);
            sql = "INSERT IGNORE INTO warehouse_info (warehouseID, lat, lon) VALUES "
                    + "('1', 37.766345, -122.512029),"
                    + "('2', 37.797750, -122.408731),"
                    + "('3', 37.711729, -122.427705)";
            statement.executeUpdate(sql);
            // 3 warehouses, each with 50 robots and 50 drones
            sql = "INSERT IGNORE INTO workers (workerID, workerType, warehouse) VALUES (?, ?, ?)";
            PreparedStatement ps = IDMService.getCon().prepareStatement(sql);
            int workerID = 1;
            for (int wh = 1; wh <= 3; wh++) {
                for (int i = 0; i < 50; i++) {
                    ps.setInt(1, workerID);
                    ps.setString(2, "ROBOT");
                    ps.setString(3, String.valueOf(wh));
                    ps.execute();
                    workerID++;
                }
                for (int i = 0; i < 50; i++) {
                    ps.setInt(1, workerID);
                    ps.setString(2, "DRONE");
                    ps.setString(3, String.valueOf(wh));
                    ps.execute();
                    workerID++;
                }
            }
            sql = "UPDATE IGNORE workers SET workerLat = 37.766345, workerLon = -122.512029 WHERE warehouse = 1";
            statement.execute(sql);
            sql = "UPDATE IGNORE workers SET workerLat = 37.797750, workerLon = -122.408731 WHERE warehouse = 2";
            statement.execute(sql);
            sql = "UPDATE IGNORE workers SET workerLat = 37.711729, workerLon = -122.427705 WHERE warehouse = 3";
            statement.execute(sql);

            // Step 5 Create database events
            sql = "SET GLOBAL event_scheduler = ON";
            statement.executeUpdate(sql);
            sql = "CREATE EVENT IF NOT EXISTS releaseHold "
                    + "ON SCHEDULE EVERY ? SECOND "
                    + "DO UPDATE workers SET holdFor = NULL WHERE holdFor IS NOT NULL "
                    + "AND holdTime < NOW()";
            ps = IDMService.getCon().prepareStatement(sql);
            ps.setInt(1, UPDATE_INTERVAL);
            ps.execute();

            // Step 6 Generate fake data
            if (fakeData) {
                generateFakeData();
            }

            ServiceLogger.LOGGER.config("Database table setup successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void generateFakeData() {
        try {
            // Connect to MySQL.
            Connection conn = IDMService.getCon();
            Statement statement = conn.createStatement();
            String sql = null;

            // fake user:  email: xuanli@gmail.com   password: 1234
            sql = "INSERT IGNORE INTO users (email, salt, pword)"
                    + "VALUE ('xuanli@gmail.com', 'faebc446', 'e91cbf2be1aedc3c0c1b6a94d5652c1d67ee67d1f592096b56348b5a577cb217743e3806ae8557c2c83f506734bda5e885b8cb6c3b8f8fa38c984062400daf94')";
            statement.executeUpdate(sql);

            // fake order 1
            sql = "INSERT IGNORE INTO package_info (packageID, pkgLength, pkgWidth, pkgHeight, pkgWeight, pkgFrom, pkgTo, pkgNotes) "
                    + "VALUES (1, 5.1, 6.2, 7.3, 150.5, 'from address 1', 'to address 1', 'it is a gift')";
            statement.executeUpdate(sql);
            sql = "INSERT IGNORE INTO delivery_info (deliveryID, deliveryType, deliveryStatus, cost) "
                    + "VALUES (1, 3, 0, 35.5)";
            statement.executeUpdate(sql);
            sql = "INSERT IGNORE INTO location_info (locationID, currentLat, currentLon, destinationLat, destinationLon) "
                    + "VALUES (1, 37.715342, -122.463503, 38.931386, -121.038749)";
            statement.executeUpdate(sql);
            sql = "INSERT IGNORE INTO orders (orderID, email, package, delivery, location, worker) "
                    + "VALUES (1, 'xuanli@gmail.com', 1, 1, 1, 1)";
            statement.executeUpdate(sql);

            // fake order 2
            sql = "INSERT IGNORE INTO package_info (packageID, pkgLength, pkgWidth, pkgHeight, pkgWeight, pkgFrom, pkgTo, pkgNotes) "
                    + "VALUES (2, 21.4, 28.9, 12.7, 320.5, 'from address 2', 'to address 2', 'happy birthday')";
            statement.executeUpdate(sql);
            sql = "INSERT IGNORE INTO delivery_info (deliveryID, deliveryType, deliveryStatus, cost) "
                    + "VALUES (2, 4, 1, 79.4)";
            statement.executeUpdate(sql);
            sql = "INSERT IGNORE INTO location_info (locationID, currentLat, currentLon, destinationLat, destinationLon) "
                    + "VALUES (2, 36.807364, -121.983462, 39.075143, -122.673974)";
            statement.executeUpdate(sql);
            sql = "INSERT IGNORE INTO orders (orderID, email, package, delivery, location, worker) "
                    + "VALUES (2, 'xuanli@gmail.com', 2, 2, 2, 2)";
            statement.executeUpdate(sql);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
