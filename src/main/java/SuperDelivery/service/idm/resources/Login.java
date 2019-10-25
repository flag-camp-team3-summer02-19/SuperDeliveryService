package SuperDelivery.service.idm.resources;

import SuperDelivery.service.idm.logger.ServiceLogger;
import SuperDelivery.service.idm.models.LoginResponseModel;
import SuperDelivery.service.idm.models.RegisterRequestModel;
import SuperDelivery.service.idm.security.Session;
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

import static SuperDelivery.service.idm.core.Helper.*;

@Path("login")
public class Login {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(String jsonText, @Context HttpHeaders headers) {
        ObjectMapper mapper = new ObjectMapper();
        RegisterRequestModel requestModel;
        LoginResponseModel lrm = new LoginResponseModel();

        try{

            requestModel = mapper.readValue(jsonText, RegisterRequestModel.class);
            String email=requestModel.getEmail();

            char[] password = requestModel.getPassword();

            if(!existEmail(email)){
                lrm.setResultCode(220);
                lrm.setMessage("User not found.");
                return Response.status(Response.Status.OK).entity(lrm).build();
            }
            if(existEmail(email)){
                String recordPword = getPword(email);
                ServiceLogger.LOGGER.info("record pword is "+recordPword);
                String salt = getSalt(email);
                ServiceLogger.LOGGER.info("record salt is "+salt);
                String inputPword = getHashedPword(password, salt);
                ServiceLogger.LOGGER.info("user input pword is "+inputPword);

                if(recordPword.equals(inputPword)){                         //case 120

                    //check if another session of this user is active and revoke it
                    revokeOldSession(email);

                    lrm.setResultCode(120);
                    lrm.setMessage("User logged in successfully.");
                    Session s = Session.createSession(email);
                    insertToSession(s);
                    String sessionID = s.getSessionID().toString();
                    lrm.setSessionID(sessionID);
                    return Response.status(Response.Status.OK).entity(lrm).build();
                }
                else{                                                       //case 11
                    lrm.setResultCode(230);
                    lrm.setMessage("Passwords do not match.");
                    return Response.status(Response.Status.OK).entity(lrm).build();
                }

            }


        } catch (IOException e) {

            e.printStackTrace();
            if (e instanceof JsonMappingException) {                        //case -2
                System.err.println("Could not map JSON to POJO.");
                lrm.setResultCode(-2);
                lrm.setMessage("JSON Mapping Exception");
                return Response.status(Response.Status.BAD_REQUEST).entity(lrm).build();
            } else if (e instanceof JsonParseException) {                   //case -3
                System.err.println("Could not Parse JSON.");
                lrm.setResultCode(-3);
                lrm.setMessage("JSON Parse Exception");
                return Response.status(Response.Status.BAD_REQUEST).entity(lrm).build();
            } else {
                System.err.println("IOException happened.");
            }
        }
        lrm.setResultCode(-1);                                //case -1
        lrm.setMessage("Internal Server Error");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(lrm).build();

    }
}