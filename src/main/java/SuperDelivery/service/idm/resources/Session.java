package SuperDelivery.service.idm.resources;

import SuperDelivery.service.idm.models.LoginResponseModel;
import SuperDelivery.service.idm.models.SessionRequestModel;
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

import static SuperDelivery.service.idm.core.HelperXuan.isSessionValid;

@Path("session")
public class Session {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sessionUser(String jsonText, @Context HttpHeaders headers) {
        ObjectMapper mapper = new ObjectMapper();
        SessionRequestModel requestModel;
        LoginResponseModel responseModel = new LoginResponseModel();

        try {
            requestModel = mapper.readValue(jsonText, SessionRequestModel.class);
            String sessionID = requestModel.getSessionID();

            // Check if session is still valid and update database accordingly
            if (isSessionValid(sessionID)) {
                responseModel.setResultCode(130);
                responseModel.setMessage("Session is valid.");
                responseModel.setSessionID(sessionID);
                return Response.status(Response.Status.OK).entity(responseModel).build();
            } else {
                responseModel.setResultCode(240);
                responseModel.setMessage("Session times out.");
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
            }
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
    }
}
