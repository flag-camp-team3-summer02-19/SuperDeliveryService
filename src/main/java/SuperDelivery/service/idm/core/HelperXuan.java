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
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.*;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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
                        LocationLatLonBuilder latLonBuilder = new LocationLatLonBuilder();
                        latLonBuilder.setLat(rs.getBigDecimal("currentLat"));
                        latLonBuilder.setLon(rs.getBigDecimal("currentLon"));
                        builder.setCurrentLocLatLon(latLonBuilder.build());
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
                latLonBuilder.setLat(rs.getBigDecimal("currentLat"));
                latLonBuilder.setLon(rs.getBigDecimal("currentLon"));
                builder.setCurrent(latLonBuilder.build());
                // set destination location
                latLonBuilder.setLat(rs.getBigDecimal("destinationLat"));
                latLonBuilder.setLon(rs.getBigDecimal("destinationLon"));
                builder.setDestination(latLonBuilder.build());
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
        return builder.build();
    }

    public static void logoutUser(String sessionID) {
        try {
            String query = "UPDATE sessions SET status = ? WHERE sessionID = ?";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            ps.setInt(1, Session.CLOSED);
            ps.setString(2, sessionID);
            ServiceLogger.LOGGER.info("Trying update: " + ps.toString());
            ps.execute();
            ServiceLogger.LOGGER.info("Session updates successfully.");
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
    }



    private static double computeDistanceBetween(LocationLatLon org, LocationLatLon dst, boolean geodesic) {
        if (geodesic) {
            // Compute the the shortest path (geodesic) between two points (unit: meter).
            return geodesicDistance(org, dst);
        } else {
            // Compute the the real walking path between two points (unit: meter).
            GeoApiContext context = IDMService.getGeoApiContext();
            LatLng orgLatLon = new LatLng(org.getLat().doubleValue(), org.getLon().doubleValue());
            LatLng dstLatLon = new LatLng(dst.getLat().doubleValue(), dst.getLon().doubleValue());
            try {
                DistanceMatrix result = DistanceMatrixApi.newRequest(context)
                        .origins(orgLatLon)
                        .destinations(dstLatLon)
                        .mode(TravelMode.WALKING)
                        .await();
                return result.rows[0].elements[0].distance.inMeters;
            } catch (Exception e) {
                ServiceLogger.LOGGER.warning("Google map Distance Matrix API cannot compute distance.");
                e.printStackTrace();
            }
        }
        return -1;
    }

    private static LocationLatLon getLatLon(String address) {
        GeoApiContext context = IDMService.getGeoApiContext();
        LocationLatLonBuilder builder = new LocationLatLonBuilder();

        // Get latitude and longitude from input address
        try {
            GeocodingResult[] result = GeocodingApi.newRequest(context)
                    .address(address)
                    .language("en")
                    .region("us")
                    .await();
            builder.setLat(BigDecimal.valueOf(result[0].geometry.location.lat));
            builder.setLon(BigDecimal.valueOf(result[0].geometry.location.lng));
        } catch (Exception e) {
            ServiceLogger.LOGGER.warning("Google map API cannot interpret address: " + address);
            e.printStackTrace();
        }
        return builder.build();
    }

    private static double geodesicDistance(LocationLatLon p1, LocationLatLon p2) {
        double lat1 = Math.toRadians(p1.getLat().doubleValue());
        double lon1 = Math.toRadians(p1.getLon().doubleValue());
        double lat2 = Math.toRadians(p2.getLat().doubleValue());
        double lon2 = Math.toRadians(p2.getLon().doubleValue());

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in meters, used in JavaScript Geometry API
        double r = 6378137;
        return (c * r);
    }

}
