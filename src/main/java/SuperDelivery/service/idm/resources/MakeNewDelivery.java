package SuperDelivery.service.idm.resources;

import SuperDelivery.service.idm.logger.ServiceLogger;
import SuperDelivery.service.idm.models.DeliveryMethods;
import SuperDelivery.service.idm.models.MakeNewDeliveryRequestModel;
import SuperDelivery.service.idm.models.MakeNewDeliveryResponseModel;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

import static SuperDelivery.service.idm.core.HelperXuan.isSessionValid;
import static SuperDelivery.service.idm.core.HelperXuan.getDeliveryMethods;

@Path("makeNewDelivery")
public class MakeNewDelivery {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deliveryMethods(String jsonText, @Context HttpHeaders headers) {
        ObjectMapper mapper = new ObjectMapper();
        MakeNewDeliveryRequestModel requestModel;
        MakeNewDeliveryResponseModel responseModel = new MakeNewDeliveryResponseModel();

        try {
            requestModel = mapper.readValue(jsonText, MakeNewDeliveryRequestModel.class);
            List<String> sessionIDs = headers.getRequestHeader("sessionID");
            if (sessionIDs != null) {
                ServiceLogger.LOGGER.info("sessionID found in request header");
                String sessionID = sessionIDs.get(0);
                if (isSessionValid(sessionID)) {
                    ServiceLogger.LOGGER.info("Session is valid");
                    DeliveryMethods methods = getDeliveryMethods(requestModel.getPackageInfo(), sessionID);
                    if (methods != null) {
                        ServiceLogger.LOGGER.info("Delivery methods found successfully");
                        responseModel.setResultCode(150);
                        responseModel.setMessage("Delivery methods found successfully");
                        responseModel.setMethods(methods);
                        return Response.status(Response.Status.OK).entity(responseModel).build();
                    } else {
                        ServiceLogger.LOGGER.info("No available workers in your nearest warehouse now");
                        responseModel.setResultCode(270);
                        responseModel.setMessage("No available workers in your nearest warehouse now.");
                        return Response.status(Response.Status.OK).entity(responseModel).build();
                    }
                } else {
                    ServiceLogger.LOGGER.info("Session is not valid");
                    responseModel.setResultCode(280);
                    responseModel.setMessage("Session is invalid (not exist, expired, revoked or closed).");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
            } else {
                ServiceLogger.LOGGER.info("sessionID not found in request header");
                responseModel.setResultCode(290);
                responseModel.setMessage("sessionID not found in request header.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (e instanceof JsonMappingException) {
                System.err.println("Could not map JSON to POJO.");
                responseModel.setResultCode(-2);
                responseModel.setMessage("JSON Mapping Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else if (e instanceof JsonParseException) {
                System.err.println("Could not Parse JSON.");
                responseModel.setResultCode(-3);
                responseModel.setMessage("JSON Parse Exception.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else {
                System.err.println("IOException happened.");
                responseModel.setResultCode(-1);
                responseModel.setMessage("Internal Server Error.");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
            }
        }
    }

}
