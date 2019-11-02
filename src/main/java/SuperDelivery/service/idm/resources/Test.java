package SuperDelivery.service.idm.resources;

import SuperDelivery.service.idm.logger.ServiceLogger;
import SuperDelivery.service.idm.models.PlaceOrderRequestModel;
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

import static SuperDelivery.service.idm.core.HelperXuan.prepareOrder;
import static SuperDelivery.service.idm.core.HelperXuan.isSessionValid;

@Path("test")
public class Test {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response testTest(String jsonText, @Context HttpHeaders headers) {
        ObjectMapper mapper = new ObjectMapper();
        PlaceOrderRequestModel requestModel;

        try {
            requestModel = mapper.readValue(jsonText, PlaceOrderRequestModel.class);
            List<String> sessionIDs = headers.getRequestHeader("sessionID");
            if (sessionIDs != null) {
                ServiceLogger.LOGGER.info("sessionID found in request header");
                String sessionID = sessionIDs.get(0);
                if (isSessionValid(sessionID)) {
                    ServiceLogger.LOGGER.info("Session is valid");
                    if (prepareOrder(requestModel.getOrder().getPackageInfo(), requestModel.getOrder().getDeliveryInfo(), sessionID, "0")) {
                        ServiceLogger.LOGGER.info("order is placed!");
                        return Response.status(Response.Status.OK).build();
                    } else {
                        ServiceLogger.LOGGER.info("order is not placed!");
                        return Response.status(Response.Status.OK).build();
                    }
                } else {
                    ServiceLogger.LOGGER.info("Session is not valid");
                    return Response.status(Response.Status.OK).build();
                }
            } else {
                ServiceLogger.LOGGER.info("sessionID not found in request header");
                return Response.status(Response.Status.OK).build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (e instanceof JsonMappingException) {
                System.err.println("Could not map JSON to POJO.");
                return Response.status(Response.Status.BAD_REQUEST).build();
            } else if (e instanceof JsonParseException) {
                System.err.println("Could not Parse JSON.");
                return Response.status(Response.Status.BAD_REQUEST).build();
            } else {
                System.err.println("IOException happened.");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
    }
}
