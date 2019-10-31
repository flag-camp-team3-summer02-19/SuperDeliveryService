package SuperDelivery.service.idm.constants;

import SuperDelivery.service.idm.IDMService;
import SuperDelivery.service.idm.logger.ServiceLogger;
import SuperDelivery.service.idm.models.LocationLatLon;
import SuperDelivery.service.idm.models.LocationLatLon.LocationLatLonBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DeliveryServiceInfo {
    // Based on wiki, many people tend to walk at about 1.4 m/s
    public static final double ROBOTSPEED = 1.4;  // unit: m/s
    // DJI Inspire 2 can fly at 58mph (25.9m/s) at top speed
    public static final double DRONESPEED = 22.5; // unit: m/s
    public static final double ROBOTCOST = 0.001; // unit: dollar/m
    public static final double DRONECOST = 0.008; // unit: dollar/m
    public static LocationLatLon WAREHOUSE1;
    public static LocationLatLon WAREHOUSE2;
    public static LocationLatLon WAREHOUSE3;

    static {
        try {
            LocationLatLonBuilder builder = new LocationLatLonBuilder();
            String sql = "SELECT * FROM warehouse_info ORDER BY warehouseID";
            Statement statement = IDMService.getCon().createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                builder.setLat(rs.getBigDecimal("lat")).setLon(rs.getBigDecimal("lon"));
                switch (rs.getString("warehouseID")) {
                    case "1":
                        WAREHOUSE1 = builder.build();
                        break;
                    case "2":
                        WAREHOUSE2 = builder.build();
                        break;
                    case "3":
                        WAREHOUSE3 = builder.build();
                        break;
                    default:
                        ServiceLogger.LOGGER.warning("Database (table: warehouse_info) has more than 3 warehouses, but only used 3 warehouses here!");
                }
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
    }

}
