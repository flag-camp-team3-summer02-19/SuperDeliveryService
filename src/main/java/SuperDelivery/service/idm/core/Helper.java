package SuperDelivery.service.idm.core;

import SuperDelivery.service.idm.IDMService;
import SuperDelivery.service.idm.logger.ServiceLogger;
import SuperDelivery.service.idm.models.RegisterRequestModel;
import SuperDelivery.service.idm.security.Crypto;
import SuperDelivery.service.idm.security.Session;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static org.glassfish.grizzly.http.util.HexUtils.convert;

public class Helper {
    public static boolean existEmail(String email){
        try {
            // Construct the query
            String query =
                    "SELECT COUNT(*) FROM users WHERE email=?;";
            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);

            // Set the parameters
            ps.setString(1, email);

            // Execute query
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            rs.next();
            if(rs.getInt(1)>0){
                ServiceLogger.LOGGER.info("This email exists.");
                return true;
            }

            ServiceLogger.LOGGER.info("This email does not exist.");
            return false;
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
        return true;
    }

    public static boolean insertUserToDb(RegisterRequestModel requestModel){
        try {

            char[] pword = requestModel.getPassword();
            byte[] salt = Crypto.genSalt();
            byte[] hashedPassword = Crypto.hashPassword(pword, salt, Crypto.ITERATIONS, Crypto.KEY_LENGTH);
            String aSalt = getHashedPass(salt);
            String password = getHashedPass(hashedPassword);
            // Construct the query
            String query =
                    "INSERT INTO users (email, salt, pword) VALUES (?, ?, ?);";
            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            // Set the parameters
            ps.setString(1, requestModel.getEmail());
            ps.setString(2, aSalt);
            ps.setString(3, password);

            // Execute query
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ps.execute();
            ServiceLogger.LOGGER.info("User registered successfully");
            return true;
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Unable to insert User " + requestModel.getEmail());
            e.printStackTrace();
        }
        return false;
    }

    private static String getHashedPass(byte[] hashedPassword) {
        StringBuffer buf = new StringBuffer();
        for (byte b : hashedPassword) {
            buf.append(format(Integer.toHexString(Byte.toUnsignedInt(b))));
        }
        return buf.toString();
    }

    private static String format(String binS) {
        int length = 2 - binS.length();
        char[] padArray = new char[length];
        Arrays.fill(padArray, '0');
        String padString = new String(padArray);
        return padString + binS;
    }

    public static String getPword(String email){
        try {
            // Construct the query
            String query =
                    "SELECT pword FROM users WHERE email = ?;";
            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            // Set the parameters
            ps.setString(1, email);

            // Execute query
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            rs.next();
            return rs.getString(1);
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Unable to get pword " + email);
            e.printStackTrace();
        }
        return null;
    }

    public static String getSalt(String email){
        try {
            // Construct the query
            String query =
                    "SELECT salt FROM users WHERE email = ?;";
            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            // Set the parameters
            ps.setString(1, email);

            // Execute query
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            rs.next();
            return rs.getString(1);
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Unable to get pword " + email);
            e.printStackTrace();
        }
        return null;
    }

    public static String getHashedPword(char[] password,String salt){
        byte[] recordSalt = convert(salt);
        byte[] hashedPassword = Crypto.hashPassword(password, recordSalt, Crypto.ITERATIONS, Crypto.KEY_LENGTH);
        return getHashedPass(hashedPassword);
    }

    public static void insertToSession(Session s){
        try {
            // Construct the query
            String query =
                    "INSERT INTO sessions (email, sessionID, status, timeCreated, lastUsed, exprTime) VALUES (?, ?, ?, ?, ?, ?);";
            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            // Set the parameters
            ps.setString(1, s.getEmail());
            ps.setString(2, s.getSessionID().toString());
            ps.setInt(3,s.getStatus());
            ps.setTimestamp(4,s.getTimeCreated());
            ps.setTimestamp(5,s.getLastUsed());
            ps.setTimestamp(6,s.getExprTime());

            // Execute query
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ps.execute();
            ServiceLogger.LOGGER.info("Query succeeded.");

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Unable to insert into session " + s.getEmail());
            e.printStackTrace();
        }
    }

    public static void revokeOldSession(String email){
        try {
            // Construct the query
            String query =
                    "SELECT sessionID FROM sessions WHERE email=? AND status=1;";
            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);

            // Set the parameters
            ps.setString(1, email);

            // Execute query
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            if (rs.next()) {
                String sessionID = rs.getString(1);
                ServiceLogger.LOGGER.info("This session exists.");
                String query2 = "UPDATE sessions SET status=? WHERE sessionID=?;";
                PreparedStatement ps2 = IDMService.getCon().prepareStatement(query2);
                ps2.setInt(1, Session.REVOKED);
                ps2.setString(2,sessionID);
                ps2.execute();
            } else {
                ServiceLogger.LOGGER.info("This session does not exist.");
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error during query.");
            e.printStackTrace();
        }
    }
}
