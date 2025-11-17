package org.eclipse.xpanse.modules.policy.policyman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.eclipse.xpanse.modules.policy.PolicyManager;
import org.eclipse.xpanse.modules.policy.policyman.config.PolicyManProperties;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.AdminApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesEvaluationApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesValidateApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalCmdList;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalResult;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.SystemStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;

@ContextConfiguration(classes = {PolicyManager.class, PolicyManProperties.class})
@TestPropertySource(properties = {"xpanse.policy-man.endpoint=http://localhost:9090"})
@Import(RefreshAutoConfiguration.class)
@ExtendWith(SpringExtension.class)
class PolicyManagerTest {

    @MockitoBean private AdminApi mockAdminApi;
    @MockitoBean private PoliciesValidateApi mockPoliciesValidateApi;
    @MockitoBean private PoliciesEvaluationApi mockPoliciesEvaluationApi;

    @Autowired private PolicyManager policyManagerUnderTest;

    @Test
    void testGetPolicyManStatus() {
        // Setup
        final BackendSystemStatus expectedResult = new BackendSystemStatus();
        expectedResult.setBackendSystemType(BackendSystemType.POLICY_MAN);
        expectedResult.setName(BackendSystemType.POLICY_MAN.toValue());
        expectedResult.setHealthStatus(HealthStatus.OK);
        expectedResult.setEndpoint("http://localhost:9090");

        // Configure AdminApi.healthGet(...).
        final SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(
                org.eclipse.xpanse.modules.policy.policyman.generated.model.HealthStatus.healthOK);
        when(mockAdminApi.healthGet()).thenReturn(systemStatus);

        // Run the test
        final BackendSystemStatus result = policyManagerUnderTest.getPolicyManStatus();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetPolicyManStatus_AdminApiThrowsRestClientException() {
        // Setup
        final BackendSystemStatus expectedResult = new BackendSystemStatus();
        expectedResult.setBackendSystemType(BackendSystemType.POLICY_MAN);
        expectedResult.setName(BackendSystemType.POLICY_MAN.toValue());
        expectedResult.setHealthStatus(HealthStatus.NOK);
        expectedResult.setEndpoint("http://localhost:9090");

        when(mockAdminApi.healthGet()).thenThrow(RestClientException.class);

        // Run the test
        final BackendSystemStatus result = policyManagerUnderTest.getPolicyManStatus();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testEvaluatePolicies() {
        // Setup
        final EvalResult evalResult = new EvalResult();
        evalResult.setIsSuccessful(true);
        // Configure PoliciesEvaluationApi.evaluatePoliciesPost(...).
        final EvalCmdList cmdList = new EvalCmdList();
        cmdList.setInput("input");
        cmdList.setPolicyList(List.of("policy"));
        when(mockPoliciesEvaluationApi.evaluatePoliciesPost(cmdList)).thenReturn(evalResult);
        // Run the test
        EvalResult result =
                policyManagerUnderTest.evaluatePolicies(
                        cmdList.getPolicyList(), cmdList.getInput());
        verify(mockPoliciesEvaluationApi).evaluatePoliciesPost(cmdList);
        Assertions.assertEquals(evalResult, result);
    }

    @Test
    void testEvaluatePolicies_PoliciesEvaluationFailed() {
        // Setup
        final EvalResult exceptionResult = new EvalResult();
        exceptionResult.setIsSuccessful(false);
        exceptionResult.setPolicy("policy");
        exceptionResult.setInput("input");

        // Configure PoliciesEvaluationApi.evaluatePoliciesPost(...).
        final EvalCmdList cmdList = new EvalCmdList();
        cmdList.setInput("input");
        cmdList.setPolicyList(List.of("value"));
        when(mockPoliciesEvaluationApi.evaluatePoliciesPost(cmdList)).thenReturn(exceptionResult);

        // Run the test
        EvalResult evalResult =
                policyManagerUnderTest.evaluatePolicies(
                        cmdList.getPolicyList(), cmdList.getInput());

        verify(mockPoliciesEvaluationApi).evaluatePoliciesPost(cmdList);

        Assertions.assertEquals(exceptionResult, evalResult);
    }

    @Test
    void testEvaluatePolicies_ThrowsRestClientException() {
        // Setup
        // Configure PoliciesEvaluationApi.evaluatePoliciesPost(...).
        final EvalCmdList cmdList = new EvalCmdList();
        cmdList.setInput("input");
        cmdList.setPolicyList(List.of("value"));
        when(mockPoliciesEvaluationApi.evaluatePoliciesPost(cmdList))
                .thenThrow(new RestClientException("error"));

        // Run the test
        Assertions.assertThrows(
                RestClientException.class,
                () -> policyManagerUnderTest.evaluatePolicies(List.of("value"), "input"));
    }
}
