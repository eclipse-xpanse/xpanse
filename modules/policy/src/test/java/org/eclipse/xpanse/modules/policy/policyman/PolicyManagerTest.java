package org.eclipse.xpanse.modules.policy.policyman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesEvaluationFailedException;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.AdminApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesEvaluationApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesValidateApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalCmdList;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalResult;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.SystemStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class PolicyManagerTest {

    @Mock
    private AdminApi mockAdminApi;
    @Mock
    private PoliciesValidateApi mockPoliciesValidateApi;
    @Mock
    private PoliciesEvaluationApi mockPoliciesEvaluationApi;

    @InjectMocks
    private PolicyManager policyManagerUnderTest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(policyManagerUnderTest, "policyManBaseUrl", "endpoint");
    }

    @Test
    void testGetPolicyManStatus() {
        // Setup
        final BackendSystemStatus expectedResult = new BackendSystemStatus();
        expectedResult.setBackendSystemType(BackendSystemType.POLICY_MAN);
        expectedResult.setName(BackendSystemType.POLICY_MAN.toValue());
        expectedResult.setHealthStatus(HealthStatus.OK);
        expectedResult.setEndpoint("endpoint");

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
        expectedResult.setEndpoint("endpoint");

        when(mockAdminApi.healthGet()).thenThrow(RestClientException.class);

        // Run the test
        final BackendSystemStatus result = policyManagerUnderTest.getPolicyManStatus();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testEvaluatePolicies() {
        // Setup
        // Configure PoliciesEvaluationApi.evaluatePoliciesPost(...).
        final EvalResult evalResult = new EvalResult();
        evalResult.setIsSuccessful(true);
        final EvalCmdList cmdList = new EvalCmdList();
        cmdList.setInput("input");
        cmdList.setPolicyList(List.of("policy"));
        when(mockPoliciesEvaluationApi.evaluatePoliciesPost(cmdList)).thenReturn(evalResult);

        // Run the test
        policyManagerUnderTest.evaluatePolicies(cmdList.getPolicyList(), cmdList.getInput());

    }

    @Test
    void testEvaluatePolicies_PoliciesEvaluationFailed() {
        // Setup
        final EvalResult evalResult = new EvalResult();
        evalResult.setIsSuccessful(false);
        evalResult.setPolicy("policy");
        evalResult.setInput("input");

        // Configure PoliciesEvaluationApi.evaluatePoliciesPost(...).
        final EvalCmdList cmdList = new EvalCmdList();
        cmdList.setInput("input");
        cmdList.setPolicyList(List.of("value"));
        when(mockPoliciesEvaluationApi.evaluatePoliciesPost(cmdList))
                .thenReturn(evalResult);

        // Run the test
        Assertions.assertThrows(PoliciesEvaluationFailedException.class,
                () -> policyManagerUnderTest.evaluatePolicies(List.of("value"), "input"));
    }

    @Test
    void testEvaluatePolicies_PoliciesEvaluationApiThrowsRestClientException() {
        // Setup
        // Configure PoliciesEvaluationApi.evaluatePoliciesPost(...).
        final EvalCmdList cmdList = new EvalCmdList();
        cmdList.setInput("input");
        cmdList.setPolicyList(List.of("value"));
        when(mockPoliciesEvaluationApi.evaluatePoliciesPost(cmdList))
                .thenThrow(new RestClientException("error"));

        // Run the test
        Assertions.assertThrows(PoliciesEvaluationFailedException.class,
                () -> policyManagerUnderTest.evaluatePolicies(List.of("value"), "input"));
    }
}
