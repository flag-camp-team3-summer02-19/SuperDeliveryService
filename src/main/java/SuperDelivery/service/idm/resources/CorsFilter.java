package SuperDelivery.service.idm.resources;

import javax.ws.rs.container.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    // Method for ContainerRequestFilter
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (isPreflightRequest(requestContext)) {
            requestContext.abortWith(Response.status(Response.Status.OK).build());
        }
    }

    // A preflight request is an OPTIONS request with an Origin header
    private static boolean isPreflightRequest (ContainerRequestContext requestContext) {
        return requestContext.getHeaderString("Origin") != null
                && requestContext.getMethod().equalsIgnoreCase("OPTIONS");
    }

    // Method for ContainerResponseFilter
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // If there is no Origin header, it is not a cross origin request and we don't need to do anything
        if (requestContext.getHeaderString("Origin") == null) {
            return;
        }

        // We need to add these CORS headers in the response
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "http://localhost:3006");
    }
}
