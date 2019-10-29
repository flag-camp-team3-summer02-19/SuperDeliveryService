package SuperDelivery.service.idm.resources;

import SuperDelivery.service.idm.logger.ServiceLogger;
import SuperDelivery.service.idm.models.RegisterResponseModel;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static SuperDelivery.service.idm.core.HelperXuan.isSessionValid;
import static SuperDelivery.service.idm.core.HelperXuan.logoutUser;

@Path("logout")
public class Logout {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@Context HttpHeaders headers) {
        RegisterResponseModel responseModel = new RegisterResponseModel();

        try {
            List<String> sessionIDs = headers.getRequestHeader("sessionID");
            if (sessionIDs != null) {
                ServiceLogger.LOGGER.info("sessionID found in request header");
                String sessionID = sessionIDs.get(0);
                if (isSessionValid(sessionID)) {
                    ServiceLogger.LOGGER.info("Session is valid");
                    logoutUser(sessionID);
                    responseModel.setResultCode(140);
                    responseModel.setMessage("User logged out successfully.");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                } else {
                    ServiceLogger.LOGGER.info("Session is not valid");
                    responseModel.setResultCode(250);
                    responseModel.setMessage("Session is invalid (not exist, expired, revoked or closed).");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
            } else {
                ServiceLogger.LOGGER.info("sessionID not found in request header");
                responseModel.setResultCode(260);
                responseModel.setMessage("sessionID not found in request header.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseModel.setResultCode(-1);
            responseModel.setMessage("Internal Server Error.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
        }
    }
}
