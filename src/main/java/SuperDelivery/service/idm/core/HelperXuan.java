package SuperDelivery.service.idm.core;

import SuperDelivery.service.idm.IDMService;
import SuperDelivery.service.idm.logger.ServiceLogger;
import SuperDelivery.service.idm.models.SessionRequestModel;
import SuperDelivery.service.idm.security.Session;
import SuperDelivery.service.idm.security.Token;
import com.mysql.cj.PreparedQuery;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HelperXuan {
    public static boolean isSessionValid(SessionRequestModel requestModel) {
        try {
            String email = requestModel.getEmail();
            String sessionID = requestModel.getSessionID();
            // Construct the query
            String query = "SELECT * FROM sessions WHERE sessionID=? AND email=? AND status=1;";
            // Create the prepared statement
            PreparedStatement ps = IDMService.getCon().prepareStatement(query);
            // Set the parameters
            ps.setString(1, sessionID);
            ps.setString(2, email);
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
}
