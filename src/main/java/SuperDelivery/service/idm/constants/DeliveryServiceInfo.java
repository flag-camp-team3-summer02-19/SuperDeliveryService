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
    // Cost related to distance
    public static final double ROBOTCOST_DIST = 0.001; // unit: dollar/m
    public static final double DRONECOST_DIST = 0.008; // unit: dollar/m
    // TODO: add cost related to size and weight

    // deliveryTypeID in database (defined by frontend)
    public static int ROBOT_TYPEID;
    public static int DRONE_TYPEID;

    // 3 warehouse locations
    public static LocationLatLon WAREHOUSE1;
    public static LocationLatLon WAREHOUSE2;
    public static LocationLatLon WAREHOUSE3;

    // deliveryStatusID in database (defined by frontend)
    public static int ORDER_PLACED;
    public static int IN_PROGRESS;
    public static int DELIVERED;

    // Max worker hold time for user (5 min)
    public static final long MAXHOLD = 300000;

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

            sql = "SELECT * FROM delivery_type ORDER BY deliveryTypeID";
            statement = IDMService.getCon().createStatement();
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                switch (rs.getString("deliveryType")) {
                    case "ROBOT":
                        ROBOT_TYPEID = rs.getInt("deliveryTypeID");
                        break;
                    case "DRONE":
                        DRONE_TYPEID = rs.getInt("deliveryTypeID");
                        break;
                    default:
                        ServiceLogger.LOGGER.warning("Database (table: delivery_type) has more than 2 worker types, but only used 2 worker types here!");
                }
            }

            sql = "SELECT * FROM delivery_status ORDER BY deliveryStatusID";
            statement = IDMService.getCon().createStatement();
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                switch (rs.getString("deliveryStatus")) {
                    case "Order Placed":
                        ORDER_PLACED = rs.getInt("deliveryStatusID");
                        break;
                    case "In Progress":
                        IN_PROGRESS = rs.getInt("deliveryStatusID");
                        break;
                    case "Delivered":
                        DELIVERED = rs.getInt("deliveryStatusID");
                        break;
                    default:
                        ServiceLogger.LOGGER.warning("Database (table: delivery_status) has more than 3 status, but only used 3 status here!");
                }
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
    }
}
