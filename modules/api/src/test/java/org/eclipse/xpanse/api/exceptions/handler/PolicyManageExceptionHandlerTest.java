/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.exceptions.handler;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.eclipse.xpanse.api.config.CspPluginValidator;
import org.eclipse.xpanse.api.controllers.UserPolicyManageApi;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesEvaluationFailedException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesValidationFailedException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyDuplicateException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyNotFoundException;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.policy.UserPolicyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserPolicyManageApi.class, PolicyManageExceptionHandler.class,
        CspPluginValidator.class, PluginManager.class, OclLoader.class})
@WebMvcTest
class PolicyManageExceptionHandlerTest {
    private final UUID id = UUID.randomUUID();
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;
    @MockBean
    private UserPolicyManager userPolicyManager;
    @MockBean
    private PluginManager pluginManager;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testPoliciesValidationFailedExceptionHandler() throws Exception {
        when(userPolicyManager.getUserPolicyDetails(id))
                .thenThrow(new PoliciesValidationFailedException("test error"));

        this.mockMvc.perform(get("/xpanse/policies/{id}", id))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Policy Validation Failed"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }


    @Test
    void testPolicyNotFoundExceptionHandler() throws Exception {
        when(userPolicyManager.getUserPolicyDetails(id))
                .thenThrow(new PolicyNotFoundException("test error"));

        this.mockMvc.perform(get("/xpanse/policies/{id}", id))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Policy Not Found"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }


    @Test
    void testPolicyDuplicatesExceptionHandler() throws Exception {
        when(userPolicyManager.getUserPolicyDetails(id))
                .thenThrow(new PolicyDuplicateException("test error"));

        this.mockMvc.perform(get("/xpanse/policies/{id}", id))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Duplicate Policy"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testPoliciesEvaluationFailedExceptionHandler() throws Exception {
        when(userPolicyManager.getUserPolicyDetails(id))
                .thenThrow(new PoliciesEvaluationFailedException("test error"));

        this.mockMvc.perform(get("/xpanse/policies/{id}", id))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Policy Evaluation Failed"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

}
