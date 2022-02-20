package org.eclipse.osc.api.identity;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Collection;

@Path("/identity")
public class Identity {

    @Path("/")
    @Produces("application/json")
    @GET
    public Collection<String> getRoles() {
        return null;
    }

    @Path("/{id}")
    @Produces("application/json")
    @GET
    public String getRole(@PathParam("role") String role) {
        return null;
    }

}
