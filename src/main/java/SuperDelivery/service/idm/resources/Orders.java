package SuperDelivery.service.idm.resources;

import SuperDelivery.service.idm.logger.ServiceLogger;
import SuperDelivery.service.idm.models.OrderListResponseModel;
import SuperDelivery.service.idm.models.OrderSummary;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

import static SuperDelivery.service.idm.core.HelperXuan.getOrderList;
import static SuperDelivery.service.idm.core.HelperXuan.isSessionValid;

@Path("orders")
public class Orders {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrders(@Context HttpHeaders headers) {
        OrderListResponseModel responseModel = new OrderListResponseModel();

        try {
            // Get sessionID from http request header
            List<String> sessionIDs = headers.getRequestHeader("sessionID");
            if (sessionIDs != null) {
                ServiceLogger.LOGGER.info("sessionID found in request header");
                String sessionID = sessionIDs.get(0);
                if (isSessionValid(sessionID)) {
                    ServiceLogger.LOGGER.info("Session is valid");
                    responseModel.setStatus("OK");
                    Set<OrderSummary> orders = getOrderList(sessionID);
                    for (OrderSummary order: orders) {
                        responseModel.addOrdersSummary(order);
                    }
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                } else {
                    ServiceLogger.LOGGER.info("Session is not valid");
                    responseModel.setStatus("FAIL");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }

            } else {
                ServiceLogger.LOGGER.info("sessionID not found in request header");
                responseModel.setStatus("FAIL");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
