package SuperDelivery.service.idm.resources;

import SuperDelivery.service.idm.core.Paypal;
import SuperDelivery.service.idm.logger.ServiceLogger;
import SuperDelivery.service.idm.models.PlaceOrderRequestModel;
import SuperDelivery.service.idm.models.PlaceOrderResponseModel;
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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static SuperDelivery.service.idm.core.HelperXuan.prepareOrder;

@Path("placeOrder")
public class PlaceOrder {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response placeOrder(@Context HttpHeaders headers, String jsonText) {
        ObjectMapper mapper = new ObjectMapper();
        PlaceOrderRequestModel requestModel;
        PlaceOrderResponseModel responseModel = new PlaceOrderResponseModel();

        try{
            List<String> sessionIDs = headers.getRequestHeader("sessionID");
            String sessionID = sessionIDs.get(0);

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
            mapper.setDateFormat(dateFormat);
            requestModel = mapper.readValue(jsonText, PlaceOrderRequestModel.class);

            float cost = requestModel.getOrder().getDeliveryInfo().getCost();

            DecimalFormat df = new DecimalFormat("0.00");
            String amount=df.format(cost);

            ServiceLogger.LOGGER.info("cost is: "+amount);
            Paypal payment = new Paypal();
            ServiceLogger.LOGGER.info("creating payment...");
            Map<String,Object> map = payment.createPayment(amount);
            if( !(((String)map.get("status")).equals("success"))){
                responseModel.setResultCode(342);
                responseModel.setMessage("Create payment failed.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }

            String redirectURL = (String) map.get("redirect_url");
            int index=redirectURL.indexOf("token=");
            String token = redirectURL.substring(index+6);
            ServiceLogger.LOGGER.info("token is: "+token);

            //insert order & transaction into DB
            prepareOrder(requestModel.getOrder().getPackageInfo(), requestModel.getOrder().getDeliveryInfo(), sessionID, token);

            responseModel.setResultCode(3400);
            responseModel.setMessage("Order placed successfully.");
            responseModel.setRedirectURL(redirectURL);
            responseModel.setToken(token);
            return Response.status(Response.Status.OK).entity(responseModel).build();



        }catch (IOException e) {

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
            }
        }
        responseModel.setResultCode(-1);
        responseModel.setMessage("Internal Server Error.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
