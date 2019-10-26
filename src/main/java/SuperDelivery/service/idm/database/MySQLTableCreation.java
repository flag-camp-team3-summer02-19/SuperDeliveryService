package SuperDelivery.service.idm.database;

import SuperDelivery.service.idm.IDMService;
import SuperDelivery.service.idm.configs.ServiceConfigs;
import SuperDelivery.service.idm.logger.ServiceLogger;

import java.sql.*;

public class MySQLTableCreation {
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

                sql = "DROP TABLE IF EXISTS users";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS package_info";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS delivery_info";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS location_info";
                statement.executeUpdate(sql);
            }

            // Step 3 Create new tables if they do not exist
            sql = "CREATE TABLE IF NOT EXISTS users ("
                    + "userId int AUTO_INCREMENT,"
                    + "email VARCHAR(50) NOT NULL,"
                    + "salt VARCHAR(8) NOT NULL,"
                    + "pword VARCHAR(128) NOT NULL,"
                    + "UNIQUE (email),"
                    + "PRIMARY KEY (userId)"
                    + ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS session_status ("
                    + "statusId int NOT NULL,"
                    + "status VARCHAR(20) NOT NULL,"
                    + "PRIMARY KEY (statusId)"
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
                    + "FOREIGN KEY (status) REFERENCES session_status(statusId) ON UPDATE CASCADE ON DELETE CASCADE"
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
                    + "pkgeSize FLOAT NOT NULL,"
                    + "pkgWeight FLOAT NOT NULL,"
                    + "pkgFrom VARCHAR(255) NOT NULL,"
                    + "pkgTo VARCHAR(255) NOT NULL,"
                    + "pkgNotes VARCHAR(255) NOT NULL,"
                    + "PRIMARY KEY (packageID)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS delivery_info ("
                    + "deliveryID int AUTO_INCREMENT,"
                    + "deliveryType VARCHAR(255) NOT NULL,"
                    + "deliveryTime timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                    + "deliveryStatus int NOT NULL,"
                    + "cost FLOAT NOT NULL,"
                    + "PRIMARY KEY (deliveryID)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS location_info ("
                    + "locationID int AUTO_INCREMENT,"
                    + "currentLat FLOAT NOT NULL,"
                    + "currentLog FLOAT NOT NULL,"
                    + "destinationLat FLOAT NOT NULL,"
                    + "destinationLog FLOAT NOT NULL,"
                    + "PRIMARY KEY (locationID)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS orders ("
                    + "orderId int AUTO_INCREMENT,"
                    + "email VARCHAR(50) NOT NULL,"
                    + "orderedTime timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                    + "package int NOT NULL,"
                    + "delivery int NOT NULL,"
                    + "location int NOT NULL,"
                    + "PRIMARY KEY (orderID),"
                    + "FOREIGN KEY (email) REFERENCES users(email) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (package) REFERENCES package_info (packageID) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (delivery) REFERENCES delivery_info (deliveryID) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (location) REFERENCES location_info (locationID) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")";
            statement.executeUpdate(sql);

            // Step 4 Insert data into session_status table
            sql = "INSERT IGNORE INTO session_status (statusId, status) VALUES (1, 'ACTIVE'), (2, 'CLOSED'), (3, 'EXPIRED'), (4, 'REVOKED');";
            statement.execute(sql);

            // Step 5 Generate fake data
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
            sql = "INSERT IGNORE INTO package_info (pkgeSize, pkgWeight, pkgFrom, pkgTo, pkgNotes) "
                    + "VALUES (5, 150, 'from address 1', 'to address 1', 'it is a gift')";
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = statement.getGeneratedKeys();
            rs.next();
            int packageID = rs.getInt(1);
            sql = "INSERT IGNORE INTO delivery_info (deliveryType, deliveryStatus, cost) "
                    + "VALUES ('DRONE', 0, 35.5)";
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            rs = statement.getGeneratedKeys();
            rs.next();
            int deliveryID = rs.getInt(1);
            sql = "INSERT IGNORE INTO location_info (currentLat, currentLog, destinationLat, destinationLog) "
                    + "VALUES (37.715342, -122.463503, 38.931386, -121.038749)";
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            rs = statement.getGeneratedKeys();
            rs.next();
            int locationID = rs.getInt(1);
            sql = "INSERT IGNORE INTO orders (email, package, delivery, location) "
                    + "VALUES ('xuanli@gmail.com', ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, packageID);
            ps.setInt(2, deliveryID);
            ps.setInt(3, locationID);
            ps.execute();

            // fake order 2
            sql = "INSERT IGNORE INTO package_info (pkgeSize, pkgWeight, pkgFrom, pkgTo, pkgNotes) "
                    + "VALUES (21, 320, 'from address 2', 'to address 2', 'happy birthday')";
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            rs = statement.getGeneratedKeys();
            rs.next();
            packageID = rs.getInt(1);
            sql = "INSERT IGNORE INTO delivery_info (deliveryType, deliveryStatus, cost) "
                    + "VALUES ('ROBOT', 1, 79.4)";
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            rs = statement.getGeneratedKeys();
            rs.next();
            deliveryID = rs.getInt(1);
            sql = "INSERT IGNORE INTO location_info (currentLat, currentLog, destinationLat, destinationLog) "
                    + "VALUES (36.807364, -121.983462, 39.075143, -122.673974)";
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            rs = statement.getGeneratedKeys();
            rs.next();
            locationID = rs.getInt(1);
            sql = "INSERT IGNORE INTO orders (email, package, delivery, location) "
                    + "VALUES ('xuanli@gmail.com', ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, packageID);
            ps.setInt(2, deliveryID);
            ps.setInt(3, locationID);
            ps.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
