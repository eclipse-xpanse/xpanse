/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.exceptions.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eclipse.xpanse.api.controllers.PolicyManageApi;
import org.eclipse.xpanse.modules.models.policy.PolicyQueryRequest;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesEvaluationFailedException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesValidationFailedException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyDuplicateException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyNotFoundException;
import org.eclipse.xpanse.modules.policy.policyman.PolicyManager;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
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
@ContextConfiguration(classes = {PolicyManageApi.class, PolicyManager.class,
        PolicyManageExceptionHandler.class, IdentityProviderManager.class})
@WebMvcTest
class PolicyManageExceptionHandlerTest {

    @Autowired
    private WebApplicationContext context;
    @MockBean
    private PolicyManager policyManager;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testPoliciesValidationFailedExceptionHandler() throws Exception {
        when(policyManager.listPolicies(any(PolicyQueryRequest.class)))
                .thenThrow(new PoliciesValidationFailedException("test error"));

        this.mockMvc.perform(get("/xpanse/policies"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Policy Validation Failed"))
                .andExpect(jsonPath("$.details[0]").value(
                        "Policy is invalid. Error reasons: test error"));
    }


    @Test
    void testPolicyNotFoundExceptionHandler() throws Exception {
        when(policyManager.listPolicies(any(PolicyQueryRequest.class)))
                .thenThrow(new PolicyNotFoundException("test error"));

        this.mockMvc.perform(get("/xpanse/policies"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Policy Not Found"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }


    @Test
    void testPolicyDuplicatesExceptionHandler() throws Exception {
        when(policyManager.listPolicies(any(PolicyQueryRequest.class)))
                .thenThrow(new PolicyDuplicateException("test error"));

        this.mockMvc.perform(get("/xpanse/policies"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Duplicate Policy"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testPoliciesEvaluationFailedExceptionHandler() throws Exception {
        when(policyManager.listPolicies(any(PolicyQueryRequest.class)))
                .thenThrow(new PoliciesEvaluationFailedException("test error"));

        this.mockMvc.perform(get("/xpanse/policies"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Policy Evaluation Failed"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

}
