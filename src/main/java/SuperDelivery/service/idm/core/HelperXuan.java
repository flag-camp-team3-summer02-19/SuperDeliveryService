package SuperDelivery.service.idm.core;

import SuperDelivery.service.idm.IDMService;
import SuperDelivery.service.idm.logger.ServiceLogger;
import SuperDelivery.service.idm.models.*;
import SuperDelivery.service.idm.models.DeliveryInfo.DeliveryInfoBuilder;
import SuperDelivery.service.idm.models.DeliveryMethods.DeliveryMethodsBuilder;
import SuperDelivery.service.idm.models.LocationInfo.LocationInfoBuilder;
import SuperDelivery.service.idm.models.LocationLatLon.LocationLatLonBuilder;
import SuperDelivery.service.idm.models.OrderSummary.OrderSummaryBuilder;
import SuperDelivery.service.idm.models.PackageInfo.PackageInfoBuilder;
import SuperDelivery.service.idm.security.Session;
import SuperDelivery.service.idm.security.Token;
import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.*;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import static SuperDelivery.service.idm.constants.DeliveryServiceInfo.*;

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

    private static String getEmail (String sessionID) {
        String email = null;
        try {
            // Construct the query
            String query = "SELECT email FROM sessions WHERE sessionID = ?";
            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            // Set the parameters
            ps.setString(1,sessionID);
            // Execute query
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            rs.next();
            email = rs.getString("email");
            ServiceLogger.LOGGER.info("Query succeeded, user email: " + email);
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
        return email;
    }

    public static Set<OrderSummary> getOrderList(String sessionID) {
        Set<OrderSummary> orders = new HashSet<>();
        String email = getEmail(sessionID);

        try {
            // Get orderIDs for current user
            Set<Integer> orderIDs = getOrderIDs(email);
            for (int orderID : orderIDs) {
                OrderSummaryBuilder builder = new OrderSummaryBuilder();
                builder.setOrderID(orderID);

                ServiceLogger.LOGGER.info("Trying to retrieve order " + orderID);

                // Get orderedTime, packageID, deliveryID, and locationID
                String query = "SELECT orderedTime, package, delivery, location FROM orders WHERE orderID = ?";
                PreparedStatement ps = IDMService.getCon().prepareStatement(query);
                ps.setInt(1, orderID);
                ResultSet rs = ps.executeQuery();

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

                    // Update delivery and location information for this order
                    updateOrder(orderID);
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
            String query = "SELECT orderID FROM orders WHERE email=? AND transactionID IS NOT NULL";
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

    public static void updateOrder(int orderID) {
        try {
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            // Fetch IDs and ordered time
            String query = "SELECT orderedTime, package, delivery, location, worker FROM orders WHERE orderID = ?";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            ps.setInt(1, orderID);
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            rs.next();
            Timestamp orderedTime = rs.getTimestamp("orderedTime");
            int packageID = rs.getInt("package");
            int deliveryID = rs.getInt("delivery");
            int locationID = rs.getInt("location");
            int workerID = rs.getInt("worker");
            if (workerID == 0) {
                ServiceLogger.LOGGER.info("This order is completed and has already been updated before.");
                return;
            }

            // Fetch package origin and destination location
            query = "SELECT pkgFrom, pkgTo FROM package_info WHERE packageID = ?";
            ps = IDMService.getCon().prepareStatement(query);
            ps.setInt(1, packageID);
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            rs.next();
            LocationLatLon orgLatLon = getLatLon(rs.getString("pkgFrom"));
            LocationLatLon dstLatLon = getLatLon(rs.getString("pkgTo"));

            // Get delivery time and update delivery status
            query = "SELECT deliveryTime FROM delivery_info WHERE deliveryID = ?";
            ps = IDMService.getCon().prepareStatement(query);
            ps.setInt(1, deliveryID);
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            rs.next();
            Timestamp deliveryTime = rs.getTimestamp("deliveryTime");
            updateDeliveryStatus(deliveryID, workerID);
            if (currentTime.after(deliveryTime)) {
                ServiceLogger.LOGGER.info("This order is completed. Updating...");
                query = "UPDATE orders SET worker = NULL WHERE orderID = ?";
                ps = IDMService.getCon().prepareStatement(query);
                ps.setInt(1, deliveryID);
                ps.execute();
                String query2 = "UPDATE location_info SET currentLat = ?, currentLon = ? WHERE locationID = ?";
                PreparedStatement ps2 = IDMService.getCon().prepareStatement(query2);
                ps2.setBigDecimal(1, dstLatLon.getLat());
                ps2.setBigDecimal(2, dstLatLon.getLon());
                ps2.setInt(3, locationID);
                ps2.execute();
                ServiceLogger.LOGGER.info("Update complete.");
                return;
            }

            // Get worker pickup time and its warehouse information
            query = "SELECT pickupTime, warehouse, workerType FROM workers WHERE workerID = ?";
            ps = IDMService.getCon().prepareStatement(query);
            ps.setInt(1, workerID);
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            rs.next();
            Timestamp pickupTime = rs.getTimestamp("pickupTime");
            Warehouse warehouse = Warehouse.getInstance(rs.getString("warehouse"));
            WorkerType workerType = WorkerType.getInstance(rs.getString("workerType"));

            // Update package and worker location
            if (currentTime.before(pickupTime)) {
                ServiceLogger.LOGGER.info("Worker is on its way to your pickup address.");
                long duration = (currentTime.getTime() - orderedTime.getTime()) / 1000;
                LocationLatLon workerLatLon = getCurrentLocation(warehouse.getLocation(), orgLatLon, duration, workerType);
                String query2 = "UPDATE workers SET workerLat = ?, workerLon = ? WHERE workerID = ?";
                PreparedStatement ps2 = IDMService.getCon().prepareStatement(query2);
                ps2.setBigDecimal(1, workerLatLon.getLat());
                ps2.setBigDecimal(2, workerLatLon.getLon());
                ps2.setInt(3, workerID);
                ps2.execute();
                ServiceLogger.LOGGER.info("Update complete.");
            } else {
                ServiceLogger.LOGGER.info("Worker is on its way to your delivery address.");
                long duration = (currentTime.getTime() - pickupTime.getTime()) / 1000;
                LocationLatLon workerLatLon = getCurrentLocation(orgLatLon, dstLatLon, duration, workerType);
                String query2 = "UPDATE workers SET workerLat = ?, workerLon = ? WHERE workerID = ?";
                PreparedStatement ps2 = IDMService.getCon().prepareStatement(query2);
                ps2.setBigDecimal(1, workerLatLon.getLat());
                ps2.setBigDecimal(2, workerLatLon.getLon());
                ps2.setInt(3, workerID);
                ps2.execute();
                query2 = "UPDATE location_info SET currentLat = ?, currentLon = ? WHERE locationID = ?";
                ps2 = IDMService.getCon().prepareStatement(query2);
                ps2.setBigDecimal(1, workerLatLon.getLat());
                ps2.setBigDecimal(2, workerLatLon.getLon());
                ps2.setInt(3, locationID);
                ps2.execute();
                ServiceLogger.LOGGER.info("Update complete.");
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
    }

    private static LocationLatLon getCurrentLocation(LocationLatLon org, LocationLatLon dst, long durationSec, WorkerType workerType) {
        LocationLatLonBuilder builder = new LocationLatLonBuilder();
        LocationLatLon current = null;
        if (workerType.isGeodesic()) {
            double ratio = durationSec / (computeDistanceBetween(org, dst, workerType.isGeodesic()) / workerType.getSpeed());
            double currentLat = org.getLat().doubleValue() + ratio * (dst.getLat().doubleValue() - org.getLat().doubleValue());
            double currentLon = org.getLon().doubleValue() + ratio * (dst.getLon().doubleValue() - org.getLon().doubleValue());
            builder.setLat(BigDecimal.valueOf(currentLat));
            builder.setLon(BigDecimal.valueOf(currentLon));
            current = builder.build();
            return current;
        } else {
            try {
                GeoApiContext context = IDMService.getGeoApiContext();
                LatLng orgLatLon = new LatLng(org.getLat().doubleValue(), org.getLon().doubleValue());
                LatLng dstLatLon = new LatLng(dst.getLat().doubleValue(), dst.getLon().doubleValue());
                DirectionsResult result = DirectionsApi.newRequest(context)
                        .origin(orgLatLon)
                        .destination(dstLatLon)
                        .mode(TravelMode.WALKING)
                        .await();
                DirectionsStep[] steps = result.routes[0].legs[0].steps;
                int nSteps = steps.length;
                int curStep = 0;
                while (curStep < nSteps) {
                    long curDuration = (long) (steps[curStep].distance.inMeters / workerType.getSpeed());
                    if (durationSec < curDuration) {
                        break;
                    }
                    durationSec -= curDuration;
                    curStep++;
                }
                if (curStep == nSteps) {
                    return dst;
                }
                double ratio = (double) durationSec / steps[curStep].duration.inSeconds;
                double currentLat = steps[curStep].startLocation.lat + ratio * (steps[curStep].endLocation.lat - steps[curStep].startLocation.lat);
                double currentLon = steps[curStep].startLocation.lng + ratio * (steps[curStep].endLocation.lng - steps[curStep].startLocation.lng);
                builder.setLat(BigDecimal.valueOf(currentLat));
                builder.setLon(BigDecimal.valueOf(currentLon));
                current = builder.build();
                return current;
            } catch (Exception e) {
                ServiceLogger.LOGGER.warning("Google map Directions API cannot compute directions.");
                e.printStackTrace();
                return current;
            }
        }
    }

    private static void updateDeliveryStatus(int deliveryID, int workerID) {
        try {
            String query = "SELECT deliveryTime FROM delivery_info WHERE deliveryID = ?";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            ps.setInt(1, deliveryID);
            ResultSet rs = ps.executeQuery();
            rs.next();
            Timestamp deliveryTime = rs.getTimestamp("deliveryTime");

            String query2 = "UPDATE delivery_info SET deliveryStatus = ? WHERE deliveryID = ?";
            PreparedStatement ps2 = IDMService.getCon().prepareStatement(query2);
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            ps2.setInt(2, deliveryID);
            if (currentTime.after(deliveryTime)) {
                ps2.setInt(1, DELIVERED);
                ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
                ps2.execute();
                ServiceLogger.LOGGER.info("DeliveryStatus update successfully.");
            } else if (isWorkerReadyToGo(workerID)) {
                ps2.setInt(1, IN_PROGRESS);
                ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
                ps2.execute();
                ServiceLogger.LOGGER.info("DeliveryStatus update successfully.");
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
    }

    // TODO: Now, worker is immediately sent out after the order is received.
    //       This part is critical, since all timestamps are related to it.
    private static  boolean isWorkerReadyToGo(int workerID) {
        return true;
    }

    public static void getOrderDetail(OrderDetailResponseModel orderDetail, int orderID) {
        try {
            String query = "SELECT package, delivery, location, worker FROM orders WHERE orderID = ?";
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
            int workerID = rs.getInt("worker");
            orderDetail.setPackageInfo(getPackageInfo(packageID));
            orderDetail.setDeliveryInfo(getDeliveryInfo(deliveryID));
            // Update delivery and location information for this order
            updateOrder(orderID);
            orderDetail.setLocationInfo(getLocationInfo(locationID, workerID));
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

    private static LocationInfo getLocationInfo (int locationID, int workerID) {
        LocationInfoBuilder builder = new LocationInfoBuilder();
        LocationLatLonBuilder latLonBuilder = new LocationLatLonBuilder();
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
            query = "SELECT workerLat, workerLon FROM workers WHERE workerID = ?";
            ps = IDMService.getCon().prepareStatement(query);
            ps.setInt(1, workerID);
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            while (rs.next()) {
                // set worker location
                latLonBuilder.setLat(rs.getBigDecimal("workerLat"));
                latLonBuilder.setLon(rs.getBigDecimal("workerLon"));
                builder.setWorker(latLonBuilder.build());
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

    public static DeliveryMethods getDeliveryMethods(PackageInfo pkgInfo, String sessionID) {
        String email = getEmail(sessionID);
        DeliveryMethods methods = null;
        DeliveryMethodsBuilder builder = new DeliveryMethodsBuilder();
        // Get lat and lon for origin and destination address;
        LocationLatLon orgLatLon = getLatLon(pkgInfo.getPkgFrom());
        LocationLatLon dstLatLon = getLatLon(pkgInfo.getPkgTo());
        // Find cheapest delivery method and fastest delivery method.
        // MVP design, need to be optimized later
        DeliveryInfo cheapestDelivery = getCheapestDelivery(orgLatLon, dstLatLon, pkgInfo);
        DeliveryInfo fastestDelivery = getFastestDelivery(orgLatLon, dstLatLon, pkgInfo);
        // First release all previously held workers
        releaseHoldWorkerForUser(email);
        if (cheapestDelivery != null && fastestDelivery != null) {
            holdWorkerForUser(email, cheapestDelivery.getWorkerID());
            holdWorkerForUser(email, fastestDelivery.getWorkerID());
            builder.setCheapest(cheapestDelivery.getCost() < fastestDelivery.getCost() ? cheapestDelivery : fastestDelivery);
            builder.setFastest(fastestDelivery.getDeliveryTime().before(cheapestDelivery.getDeliveryTime()) ? fastestDelivery : cheapestDelivery);
            methods = builder.build();
        } else if (cheapestDelivery != null) {
            holdWorkerForUser(email, cheapestDelivery.getWorkerID());
            builder.setCheapest(cheapestDelivery);
            builder.setFastest(cheapestDelivery);
            methods = builder.build();
        } else if (fastestDelivery != null) {
            holdWorkerForUser(email, fastestDelivery.getWorkerID());
            builder.setCheapest(fastestDelivery);
            builder.setFastest(fastestDelivery);
            methods = builder.build();
        }
        return methods;
    }

    private static void holdWorkerForUser(String email, int workerID) {
        try {
            String query = "UPDATE workers SET holdFor = ?, holdTime = ? WHERE workerID = ?";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis() + MAXHOLD));
            ps.setInt(3, workerID);
            ServiceLogger.LOGGER.info("Trying update: " + ps.toString());
            ps.execute();
            ServiceLogger.LOGGER.info("Workers held successfully.");
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during holding.");
            e.printStackTrace();
        }
    }

    private static void releaseHoldWorkerForUser(String email) {
        try {
            String query = "UPDATE workers SET holdFor = NULL WHERE holdFor = ?";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ServiceLogger.LOGGER.info("Trying update: " + ps.toString());
            ps.execute();
            ServiceLogger.LOGGER.info("Workers released successfully.");
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during releasing.");
            e.printStackTrace();
        }
    }

    private static DeliveryInfo getCheapestDelivery(LocationLatLon orgLatLon, LocationLatLon dstLatLon, PackageInfo pkgInfo) {
        // MVP: only check nearest robots
        WorkerType workerType = WorkerType.ROBOT;
        DeliveryInfo cheapestDelivery = null;
        Warehouse warehouse = findNearestWarehouse(orgLatLon, workerType);
        int workerID = getAvailableWorker(warehouse, workerType);
        if (workerID != -1) {
            double distance = 0;  // unit: meter
            distance += computeDistanceBetween(warehouse.getLocation(), orgLatLon, workerType.isGeodesic());
            distance += computeDistanceBetween(orgLatLon, dstLatLon, workerType.isGeodesic());
            long duration = (long) (distance / workerType.getSpeed());  // unit: second
            float cost = (float) getDeliveryCost(distance, pkgInfo, workerType);
            Timestamp deliveryTime = new Timestamp(System.currentTimeMillis() + duration * 1000);
            cheapestDelivery = new DeliveryInfoBuilder().setDeliveryType(workerType.getTypeID())
                                                        .setDeliveryTime(deliveryTime)
                                                        .setCost(cost)
                                                        .setWorkerID(workerID)
                                                        .build();
        }
        return cheapestDelivery;
    }

    private static DeliveryInfo getFastestDelivery(LocationLatLon orgLatLon, LocationLatLon dstLatLon, PackageInfo pkgInfo) {
        // MVP: only check nearest drones
        WorkerType workerType = WorkerType.DRONE;
        DeliveryInfo fastestDelivery = null;
        Warehouse warehouse = findNearestWarehouse(orgLatLon, workerType);
        int workerID = getAvailableWorker(warehouse, workerType);
        if (workerID != -1) {
            double distance = 0;  // unit: meter
            distance += computeDistanceBetween(warehouse.getLocation(), orgLatLon, workerType.isGeodesic());
            distance += computeDistanceBetween(orgLatLon, dstLatLon, workerType.isGeodesic());
            long duration = (long) (distance / workerType.getSpeed());  // unit: second
            float cost = (float) getDeliveryCost(distance, pkgInfo, workerType);
            Timestamp deliveryTime = new Timestamp(System.currentTimeMillis() + duration * 1000);
            fastestDelivery = new DeliveryInfoBuilder().setDeliveryType(workerType.getTypeID())
                                                       .setDeliveryTime(deliveryTime)
                                                       .setCost(cost)
                                                       .setWorkerID(workerID)
                                                       .build();
        }
        return fastestDelivery;
    }

    private static double getDeliveryCost(double distance, PackageInfo pkgInfo, WorkerType workerType) {
        double cost = 0;
        // cost related to distance
        cost += distance * workerType.getCostDist();
        // TODO: hwo to determine cost related to package size and weight?
        //       add unit cost constants to DeliveryServiceInfo class
        //       and connected with new cost fields in WorkerType enum

        return cost;
    }

    private static boolean isWorkerAvailable(Warehouse warehouse, WorkerType workerType) {
        return getAvailableWorker(warehouse, workerType) != -1;
    }

    private static int getAvailableWorker(Warehouse warehouse, WorkerType workerType) {
        // Get the workerID of one available worker of a specific type in a specific warehouse
        // Return -1 if no such available worker
        try {
            String query = "SELECT workerID FROM workers WHERE warehouse = ? AND workerType = ? "
                    + "AND availableTime <= NOW() AND holdFor IS NULL";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            ps.setString(1, warehouse.getDbName());
            ps.setString(2, workerType.getDbName());
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            if(rs.next()){
                int workerID = rs.getInt("workerID");
                ServiceLogger.LOGGER.info(workerType.getDbName() + "(id: " + workerID
                        + ") is available in warehouse " + warehouse.getDbName());
                return workerID;
            } else {
                ServiceLogger.LOGGER.info("There is no available " + workerType.getDbName()
                        + " in warehouse " + warehouse.getDbName());
                return -1;
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
        return -1;
    }

    private static Warehouse findNearestWarehouse(LocationLatLon loc, WorkerType workertype) {
        double minDistance = Double.MAX_VALUE;
        Warehouse nearestWarehouse = null;
        for (Warehouse warehouse : Warehouse.values()) {
            double dist = computeDistanceBetween(loc, warehouse.getLocation(), workertype.isGeodesic());
            if (dist < minDistance) {
                minDistance = dist;
                nearestWarehouse = warehouse;
            }
        }
        return nearestWarehouse;
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
                return -1;
            }
        }
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


    public static boolean prepareOrder(PackageInfo pkgInfo, DeliveryInfo dlvInfo, String sessionID, String token) {
        // Populate order information
        String email = getEmail(sessionID);
        // Get worker type
        WorkerType workerType = WorkerType.getInstance(dlvInfo.getDeliveryType());
        try {
            String query = "SELECT workerID, warehouse FROM workers WHERE holdFor = ? AND workerType = ?";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, workerType.getDbName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ServiceLogger.LOGGER.info("Hold worker exists.");
                int workerID = rs.getInt("workerID");
                // Get warehouse
                Warehouse warehouse = Warehouse.getInstance(rs.getString("warehouse"));
                LocationLatLon orgLatLon = getLatLon(pkgInfo.getPkgFrom());
                LocationLatLon dstLatLon = getLatLon(pkgInfo.getPkgTo());
                // Compute key timestamp
                double distance1 = computeDistanceBetween(warehouse.getLocation(), orgLatLon, workerType.isGeodesic());
                double distance2 = computeDistanceBetween(orgLatLon, dstLatLon, workerType.isGeodesic());
                double distance3 = computeDistanceBetween(dstLatLon, warehouse.getLocation(), workerType.isGeodesic());
                long duration1 = (long) (distance1 / workerType.getSpeed() * 1000); //unit: ms
                long duration2 = (long) (distance2 / workerType.getSpeed() * 1000);
                long duration3 = (long) (distance3 / workerType.getSpeed() * 1000);
                Timestamp orderedTime = new Timestamp(System.currentTimeMillis());
                Timestamp pickupTime = new Timestamp(orderedTime.getTime() + duration1);
                Timestamp deliveryTime = new Timestamp(pickupTime.getTime() + duration2);
                Timestamp availableTime = new Timestamp(deliveryTime.getTime() + duration3);
                // Bind the hold worker
                boolean res = bindWorker(workerID, pickupTime, availableTime);
                if (!res) {
                    return false;
                }
                // Need to release user's hold workers
                releaseHoldWorkerForUser(email);
                // Bind the package info
                int packageID = bindPackageInfo(pkgInfo);
                if (packageID == -1) {
                    return false;
                }
                // Bind the delivery info
                int deliveryID = bindDeliveryInfo(dlvInfo, deliveryTime);
                if (deliveryID == - 1) {
                    return false;
                }
                // Bind the location info
                int locationID = bindLocationInfo(pkgInfo);
                if (locationID == - 1) {
                    return false;
                }
                String query2 = "INSERT INTO orders (email, orderedTime, package, delivery, location, worker, token)"
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps2 = IDMService.getCon().prepareStatement(query2);
                ps2.setString(1, email);
                ps2.setTimestamp(2, orderedTime);
                ps2.setInt(3, packageID);
                ps2.setInt(4, deliveryID);
                ps2.setInt(5, locationID);
                ps2.setInt(6, workerID);
                ps2.setString(7, token);
                ServiceLogger.LOGGER.info("Trying insert: " + ps.toString());
                ps2.executeUpdate();
                ServiceLogger.LOGGER.warning("Order is placed successfully.");
                return true;
            } else {
                ServiceLogger.LOGGER.info("Hold worker has been released (payment not received within " + MAXHOLD/1000/60 + " min).");
                return false;
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        } catch (NullPointerException e) {
            ServiceLogger.LOGGER.warning("Please check if you input a valid worker type (WorkerType) and addresses.");
            e.printStackTrace();
        } catch (Exception e) {
            ServiceLogger.LOGGER.warning("Unexpected exception happens.");
            e.printStackTrace();
        }
        return false;
    }

    private static boolean bindWorker(int workerID, Timestamp pickupTime, Timestamp availableTime) {
        try {
            String query = "UPDATE workers SET pickupTime = ?, availableTime = ? WHERE workerID = ?";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            ps.setTimestamp(1, pickupTime);
            ps.setTimestamp(2, availableTime);
            ps.setInt(3, workerID);
            ServiceLogger.LOGGER.info("Trying update: " + ps.toString());
            ps.execute();
            ServiceLogger.LOGGER.warning("Binding worker successfully.");
            return true;
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during binding worker.");
            e.printStackTrace();
        }
        return false;
    }

    private static int bindPackageInfo(PackageInfo pkgInfo) {
        // Return packageID if insert successfully, otherwise return -1
        try {
            String query = "INSERT INTO package_info (pkgLength, pkgWidth, pkgHeight, pkgWeight, pkgFrom, pkgTo, pkgNotes) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setFloat(1, pkgInfo.getPkgLength());
            ps.setFloat(2, pkgInfo.getPkgWidth());
            ps.setFloat(3, pkgInfo.getPkgHeight());
            ps.setFloat(4, pkgInfo.getPkgWeight());
            ps.setString(5, pkgInfo.getPkgFrom());
            ps.setString(6, pkgInfo.getPkgTo());
            ps.setString(7, pkgInfo.getPkgNotes());
            ServiceLogger.LOGGER.info("Trying insert: " + ps.toString());
            ps.executeUpdate();
            ServiceLogger.LOGGER.warning("Binding package info successfully.");
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during binding package info.");
            e.printStackTrace();
        }
        return -1;
    }

    private static int bindDeliveryInfo(DeliveryInfo dlvInfo, Timestamp deliveryTime) {
        // Return deliveryID if insert successfully, otherwise return -1
        try {
            String query = "INSERT INTO delivery_info (deliveryType, deliveryTime, deliveryStatus, cost) "
                    + "VALUES (?, ?, ?, ?)";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, dlvInfo.getDeliveryType());
            ps.setTimestamp(2, deliveryTime);
            ps.setInt(3, ORDER_PLACED);
            ps.setFloat(4, dlvInfo.getCost());
            ServiceLogger.LOGGER.info("Trying insert: " + ps.toString());
            ps.executeUpdate();
            ServiceLogger.LOGGER.warning("Binding delivery info successfully.");
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during binding delivery info.");
            e.printStackTrace();
        }
        return -1;
    }

    private static int bindLocationInfo(PackageInfo pkgInfo) {
        try {
            LocationLatLon current = getLatLon(pkgInfo.getPkgFrom());
            LocationLatLon destination = getLatLon(pkgInfo.getPkgTo());
            String query = "INSERT INTO location_info (currentLat, currentLon, destinationLat, destinationLon)"
                    + "VALUES (?, ?, ?, ?)";
            PreparedStatement ps = IDMService.getCon().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setBigDecimal(1, current.getLat());
            ps.setBigDecimal(2, current.getLon());
            ps.setBigDecimal(3, destination.getLat());
            ps.setBigDecimal(4, destination.getLon());
            ServiceLogger.LOGGER.info("Trying insert: " + ps.toString());
            ps.executeUpdate();
            ServiceLogger.LOGGER.warning("Binding location info successfully.");
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during binding location info.");
            e.printStackTrace();
        }
        return -1;
    }



}
