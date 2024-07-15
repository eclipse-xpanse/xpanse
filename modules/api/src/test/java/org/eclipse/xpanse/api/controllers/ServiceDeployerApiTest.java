package org.eclipse.xpanse.api.controllers;

import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.ServiceDetailsViewManager;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ServiceDeployerApi.class)
class ServiceDeployerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeployService mockDeployService;
    @MockBean
    private UserServiceHelper mockUserServiceHelper;
    @MockBean
    private ServiceDetailsViewManager mockServiceDetailsViewManager;
    @MockBean
    private DeployServiceEntityHandler mockDeployServiceEntityHandler;
    @MockBean
    private PluginManager mockPluginManager;


}
