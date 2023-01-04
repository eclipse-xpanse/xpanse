package org.eclipse.osc.services.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.karaf.minho.boot.Minho;
import org.eclipse.osc.orchestrator.OrchestratorService;
import org.eclipse.osc.services.ocl.loader.Ocl;

import java.util.Set;

@Slf4j
@Path("/")
public class OrchestratorApi {

    @Path("/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(Ocl ocl) throws Exception {
        getOrchestrator().registerManagedService(ocl);
        return Response.ok().build();
    }

    @Path("/register/fetch")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response fetch(@HeaderParam("ocl") String oclLocation) throws Exception {
        getOrchestrator().registerManagedService(oclLocation);
        return Response.ok().build();
    }

    @Path("/health")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String health() throws Exception {
        if (getOrchestrator() == null) {
            throw new IllegalStateException("Orchestrator service is not ready");
        }
        return "ready";
    }

    @Path("/services")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String services() throws Exception {
        StringBuilder builder = new StringBuilder();
        getOrchestrator().getStorage().services().stream().forEach(service -> {
            builder.append(service).append("\n");
        });
        return builder.toString();
    }

    @Path("/start")
    @POST
    public Response start(@HeaderParam("managedServiceName") String managedServiceName) throws Exception {
        getOrchestrator().startManagedService(managedServiceName);
        return Response.ok().build();
    }

    @Path("/stop")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response stop(@HeaderParam("managedServiceName") String managedServiceName) throws Exception {
        getOrchestrator().stopManagedService(managedServiceName);
        return Response.ok().build();
    }

    @Path("/update")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@HeaderParam("managedServiceName") String managedServiceName, Ocl ocl) throws Exception {
        getOrchestrator().updateManagedService(managedServiceName, ocl);
        return Response.ok().build();
    }

    @Path("/update/fetch")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response update(@HeaderParam("managedServiceName") String managedServiceName, @HeaderParam("ocl") String oclLocation) throws Exception {
        getOrchestrator().updateManagedService(managedServiceName, oclLocation);
        return Response.ok().build();
    }

    private OrchestratorService getOrchestrator() throws Exception {
        Minho minho = Minho.getInstance();
        OrchestratorService orchestratorService = minho.getServiceRegistry().get(OrchestratorService.class);

        if (orchestratorService == null) {
            throw new IllegalStateException("Orchestrator service is not available");
        }

        return orchestratorService;
    }

}
