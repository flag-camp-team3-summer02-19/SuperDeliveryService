package SuperDelivery.service.idm.core;

import SuperDelivery.service.idm.IDMService;
import SuperDelivery.service.idm.logger.ServiceLogger;
import SuperDelivery.service.idm.models.*;
import SuperDelivery.service.idm.models.DeliveryInfo.DeliveryInfoBuilder;
import SuperDelivery.service.idm.models.OrderSummary.OrderSummaryBuilder;
import SuperDelivery.service.idm.models.PackageInfo.PackageInfoBuilder;
import SuperDelivery.service.idm.models.LocationInfo.LocationInfoBuilder;
import SuperDelivery.service.idm.models.LocationLatLon.LocationLatLonBuilder;
import SuperDelivery.service.idm.security.Session;
import SuperDelivery.service.idm.security.Token;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HelperXuan {
    public static boolean isSessionValid(String sessionID) {
        try {
            // Construct the query
            String query = "SELECT * FROM sessions WHERE sessionID=? AND status=1;";
            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            // Set the parameters
            ps.setString(1, sessionID);
            // Execute query
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            if (rs.next()) {
                ServiceLogger.LOGGER.info("This session exists.");
                // Reconstruct session object from database record
                Session session = Session.rebuildSession(rs.getString("email"),
                        Token.toToken(rs.getString("sessionID")),
                        rs.getInt("status"),
                        rs.getTimestamp("timeCreated"),
                        rs.getTimestamp("lastUsed"),
                        rs.getTimestamp("exprTime"));
                ServiceLogger.LOGGER.info("Session reconstructs successfully.");
                // Check if session time out
                if (!session.isSessionTimeOut()) {
                    ServiceLogger.LOGGER.info("Session not time out.");
                    String query2 = "UPDATE sessions SET lastUsed=? WHERE sessionID=?;";
                    PreparedStatement ps2 = IDMService.getCon().prepareStatement(query2);
                    ps2.setTimestamp(1, session.getLastUsed());
                    ps2.setString(2, session.getSessionID().toString());
                    ServiceLogger.LOGGER.info("Trying update: " + ps2.toString());
                    ps2.execute();
                    ServiceLogger.LOGGER.info("Session updates successfully.");
                    return true;
                } else {
                    ServiceLogger.LOGGER.info("Session time out.");
                    String query2 = "UPDATE sessions SET status=? WHERE sessionID=?;";
                    PreparedStatement ps2 = IDMService.getCon().prepareStatement(query2);
                    ps2.setInt(1, session.getStatus());
                    ps2.setString(2, session.getSessionID().toString());
                    ServiceLogger.LOGGER.info("Trying update: " + ps2.toString());
                    ps2.execute();
                    ServiceLogger.LOGGER.info("Session updates successfully.");
                    return false;
                }

            } else {
                ServiceLogger.LOGGER.info("This session does not exist.");
                return false;
            }

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
        return false;
    }

    public static Set<OrderSummary> getOrderList(String sessionID) {
        Set<OrderSummary> orders = new HashSet<>();

        try {
            // Construct the query
            String query = "SELECT email FROM sessions WHERE sessionID=?";
            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            // Set the parameters
            ps.setString(1,sessionID);
            // Execute query
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            rs.next();
            String email = rs.getString("email");
            ServiceLogger.LOGGER.info("Query succeeded, user email: " + email);
            // Get orderIDs for current user
            Set<Integer> orderIDs = getOrderIDs(email);
            for (int orderID : orderIDs) {
                OrderSummaryBuilder builder = new OrderSummaryBuilder();
                builder.setOrderID(orderID);

                ServiceLogger.LOGGER.info("Trying to retrieve order " + orderID);

                // Get orderedTime, packageID, deliveryID, and locationID
                query = "SELECT orderedTime, package, delivery, location FROM orders WHERE orderID = ?";
                ps = IDMService.getCon().prepareStatement(query);
                ps.setInt(1, orderID);
                rs = ps.executeQuery();

                while (rs.next()) {
                    builder.setOrderedTime(rs.getTimestamp("orderedTime"));
                    int packageID = rs.getInt("package");
                    int deliveryID = rs.getInt("delivery");
                    int locationID = rs.getInt("location");

                    // Get package from, to, and notes
                    query = "SELECT pkgFrom, pkgTo, pkgNotes FROM package_info WHERE packageID = ?";
                    ps = IDMService.getCon().prepareStatement(query);
                    ps.setInt(1, packageID);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        builder.setPkgFrom(rs.getString("pkgFrom"));
                        builder.setPkgTo(rs.getString("pkgTo"));
                        builder.setPkgNotes(rs.getString("pkgNotes"));
                    }

                    // Get delivery type and status
                    query = "SELECT deliveryType, deliveryStatus FROM delivery_info WHERE deliveryID = ?";
                    ps = IDMService.getCon().prepareStatement(query);
                    ps.setInt(1, deliveryID);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        builder.setDeliveryType(rs.getInt("deliveryType"));
                        builder.setDeliveryStatus(rs.getInt("deliveryStatus"));
                    }

                    // Get current location's lat and lon
                    // TODO: implement updateLocation() function to update package's current lat and lon location
                    //       and save them to location_info table; meanwhile, this function can also change deliveryStatus
                    //       in delivery_info table if package is delivered.
                    //       If need to provide notification to users, this function need to be run continuously at backend.
                    updateLocation();
                    query = "SELECT currentLat, currentLon FROM location_info WHERE locationID = ?";
                    ps = IDMService.getCon().prepareStatement(query);
                    ps.setInt(1, locationID);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        Map<String, Float> currentLocLatLon = new HashMap<>();
                        currentLocLatLon.put("lat", rs.getFloat("currentLat"));
                        currentLocLatLon.put("lon", rs.getFloat("currentLon"));
                        builder.setCurrentLocLatLon(currentLocLatLon);
                    }
                }
                orders.add(builder.build());
                ServiceLogger.LOGGER.info("Query succeeded.");
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
        return orders;
    }

    private static Set<Integer> getOrderIDs(String email) {
        Set<Integer> orderIDs = new HashSet<>();

        try {
            String query = "SELECT orderID FROM orders WHERE email=?";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ServiceLogger.LOGGER.info("Trying to retrieve orderIDs.");
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            while (rs.next()) {
                orderIDs.add(rs.getInt("orderID"));
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
        return orderIDs;
    }

    // TODO: implement updateLocation() function to update package's current lat and lon location
    //       and save them to location_info table; meanwhile, this function can also change deliveryStatus
    //       in delivery_info table if package is delivered.
    //       If need to provide notification to users, this function need to be run continuously at backend.
    public static void updateLocation() {

    }

    public static void getOrderDetail(OrderDetailResponseModel orderDetail, int orderID) {
        try {
            String query = "SELECT package, delivery, location FROM orders WHERE orderID = ?";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            ps.setInt(1, orderID);
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            if (!rs.next()) {
                orderDetail.setStatus("FAIL");
                return;
            }
            int packageID = rs.getInt("package");
            int deliveryID = rs.getInt("delivery");
            int locationID = rs.getInt("location");
            orderDetail.setPackageInfo(getPackageInfo(packageID));
            orderDetail.setDeliveryInfo(getDeliveryInfo(deliveryID));
            orderDetail.setLocationInfo(getLocationInfo(locationID));
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
    }

    private static PackageInfo getPackageInfo(int packageID) {
        PackageInfoBuilder builder = new PackageInfoBuilder();

        try {
            String query = "SELECT * FROM package_info WHERE packageID = ?";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            ps.setInt(1, packageID);
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            while (rs.next()) {
                builder.setPkgLength(rs.getFloat("pkgLength"));
                builder.setPkgWidth(rs.getFloat("pkgWidth"));
                builder.setPkgHeight(rs.getFloat("pkgHeight"));
                builder.setPkgWeight(rs.getFloat("pkgWeight"));
                builder.setPkgFrom(rs.getString("pkgFrom"));
                builder.setPkgTo(rs.getString("pkgTo"));
                builder.setPkgNotes(rs.getString("pkgNotes"));
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
        return builder.build();
    }

    private static DeliveryInfo getDeliveryInfo (int deliveryID) {
        DeliveryInfoBuilder builder = new DeliveryInfoBuilder();

        try {
            String query = "SELECT * FROM delivery_info WHERE deliveryID = ?";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            ps.setInt(1, deliveryID);
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            while (rs.next()) {
                builder.setDeliveryType(rs.getInt("deliveryType"));
                builder.setDeliveryTime(rs.getTimestamp("deliveryTime"));
                builder.setDeliveryStatus(rs.getInt("deliveryStatus"));
                builder.setCost(rs.getFloat("cost"));
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
        return builder.build();
    }

    private static LocationInfo getLocationInfo (int locationID) {
        LocationInfoBuilder builder = new LocationInfoBuilder();
        LocationLatLonBuilder latLonBuilder = new LocationLatLonBuilder();

        // update current location
        updateLocation();

        try {
            String query = "SELECT * FROM location_info WHERE locationID = ?";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            ps.setInt(1, locationID);
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            while (rs.next()) {
                // set current location
                latLonBuilder.setLat(rs.getFloat("currentLat"));
                latLonBuilder.setLon(rs.getFloat("currentLon"));
                builder.setCurrent(latLonBuilder.build());
                // set destination location
                latLonBuilder.setLat(rs.getFloat("destinationLat"));
                latLonBuilder.setLon(rs.getFloat("destinationLon"));
                builder.setDestination(latLonBuilder.build());
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
        return builder.build();
    }

}
