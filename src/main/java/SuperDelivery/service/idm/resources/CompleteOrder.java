package SuperDelivery.service.idm.resources;

import SuperDelivery.service.idm.models.CompleteOrderResponseModel;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static SuperDelivery.service.idm.core.Helper.checkIfTokenInTransactions;
import static SuperDelivery.service.idm.core.Helper.updateTransactionId;
import static SuperDelivery.service.idm.core.Paypal.completePayment;

@Path("orderComplete")
public class CompleteOrder {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response completeOrder(@Context HttpHeaders headers,
                                  @NotNull @QueryParam("paymentId") String paymentId,
                                  @NotNull @QueryParam("token") String token,
                                  @NotNull @QueryParam("PayerID") String PayerID) {
        CompleteOrderResponseModel responseModel = new CompleteOrderResponseModel();
        if(checkIfTokenInTransactions(token) == false){
            responseModel.setResultCode(3421);
            responseModel.setMessage("Token not found.");
            return Response.status(Response.Status.OK).entity(responseModel).build();
        }
        Map<String,Object> map = completePayment(paymentId,PayerID);
        if( !(((String)map.get("status")).equals("success"))){
            responseModel.setResultCode(3422);
            responseModel.setMessage("Payment can not be completed.");
            return Response.status(Response.Status.OK).entity(responseModel).build();
        }
        if( ((String)map.get("status")).equals("success")){

            String transactionId = (String)map.get("transactionId");
            updateTransactionId(transactionId,token);
            responseModel.setResultCode(3420);
            responseModel.setMessage("Payment is completed successfully.");
            return Response.status(Response.Status.OK).entity(responseModel).build();
        }



        responseModel.setResultCode(-1);
        responseModel.setMessage("Internal Server Error.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
