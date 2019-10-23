package SuperDelivery.service.idm.resources;

import SuperDelivery.service.idm.models.RegisterRequestModel;
import SuperDelivery.service.idm.models.RegisterResponseModel;
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
import java.util.Arrays;

import static SuperDelivery.service.idm.core.Helper.existEmail;
import static SuperDelivery.service.idm.core.Helper.insertUserToDb;

@Path("register")
public class Register {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response RegisterUser(String jsonText, @Context HttpHeaders headers){
        ObjectMapper mapper = new ObjectMapper();
        RegisterRequestModel requestModel;
        RegisterResponseModel responseModel = new RegisterResponseModel();

        try {
            requestModel = mapper.readValue(jsonText, RegisterRequestModel.class);
            String email = requestModel.getEmail();
            char[] password = requestModel.getPassword();
            if(existEmail(email)){
                responseModel.setResultCode(210);                            //case 16
                responseModel.setMessage("Email already in use.");
                Arrays.fill(password,'0');
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }

            else{
                if(insertUserToDb(requestModel)){
                    responseModel.setResultCode(110);                            //case 110
                    responseModel.setMessage("User registered successfully.");
                    return Response.status(Response.Status.OK).entity(responseModel).build();}
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
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
