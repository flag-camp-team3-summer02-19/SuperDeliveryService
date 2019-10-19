package SuperDelivery.service.idm.database;

import SuperDelivery.service.idm.IDMService;
import SuperDelivery.service.idm.configs.ServiceConfigs;
import SuperDelivery.service.idm.logger.ServiceLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MySQLTableCreation {
    public static void CreateTable() {
        try {
            // Step 1 Connect to MySQL.
            Connection conn = IDMService.getCon();

            if (conn == null) {
                return;
            }

            // Step 2 Drop tables in case they exist.
            Statement statement = conn.createStatement();
            String sql = "DROP TABLE IF EXISTS user";
            statement.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS session";
            statement.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS session_status";
            statement.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS user_address";
            statement.executeUpdate(sql);


            // Step 3 Create new tables
            sql = "CREATE TABLE users ("
                    + "user_id int AUTO_INCREMENT,"
                    + "email VARCHAR(50) NOT NULL,"
                    + "salt VARCHAR(8) NOT NULL,"
                    + "password VARCHAR(128) NOT NULL,"
                    + "PRIMARY KEY (user_id)"
                    + ")";
            statement.execute(sql);

            sql = "CREATE TABLE session_status ("
                    + "status_id int NOT NULL,"
                    + "status VARCHAR(20) NOT NULL,"
                    + "PRIMARY KEY (status_id)"
                    + ")";
            statement.execute(sql);

            sql = "CREATE TABLE session ("
                    + "session_id VARCHAR(128) NOT NULL,"
                    + "email VARCHAR(50) NOT NULL,"
                    + "status int NOT NULL,"
                    + "time_created timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                    + "last_used timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                    + "expr_time timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                    + "PRIMARY KEY (session_id),"
                    + "FOREIGN KEY (email) REFERENCES users(email) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (status) REFERENCES session_status(status_id) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")";
            statement.execute(sql);

            sql = "CREATE TABLE user_address ("
                    + "email VARCHAR(50) NOT NULL,"
                    + "street VARCHAR(128) NOT NULL,"
                    + "city VARCHAR(32) NOT NULL,"
                    + "state VARCHAR(8) NOT NULL,"
                    + "zipcode int NOT NULL,"
                    + "PRIMARY KEY (email),"
                    + "FOREIGN KEY (email) REFERENCES users (email) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")";
            statement.execute(sql);

            conn.close();
            ServiceLogger.LOGGER.config("Table created successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
