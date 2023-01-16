package org.eclipse.osc.modules.api;

import io.swagger.v3.jaxrs2.integration.OpenApiServlet;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;
import org.apache.karaf.minho.web.jetty.JettyWebContainerService;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.Objects;

public class SwaggerService implements Service {

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) throws Exception {
        JettyWebContainerService jettyWebContainerService = serviceRegistry.get(JettyWebContainerService.class);
        if (Objects.nonNull(jettyWebContainerService)) {
            // expose API definition
            ServletHolder holder = jettyWebContainerService.addServlet(OpenApiServlet.class, "/openapi/*");
            holder.setInitOrder(2);
            holder.setInitParameter("openApi.configuration.resourcePackages", "org.eclipse.osc.modules.api");

            // expose Swagger UI
            jettyWebContainerService.addServlet(SwaggerUIUnpkgServlet.class, "/swagger");
        }
    }

}
