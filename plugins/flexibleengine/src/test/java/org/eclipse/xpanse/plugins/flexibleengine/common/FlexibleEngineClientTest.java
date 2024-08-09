package org.eclipse.xpanse.plugins.flexibleengine.common;

import com.huaweicloud.sdk.core.auth.ICredential;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialsNotFoundException;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlexibleEngineClientTest {

    private FlexibleEngineClient testClient;

    @BeforeEach
    void setUp() {
        testClient = new FlexibleEngineClient();
    }

    @Test
    void testGetCredentialForClient() {
        // Setup
        final AbstractCredentialInfo credential = getCredentialDefinition("ak", "sk");
        // Run the test
        final ICredential result = testClient.getCredential(credential);
        // Verify the results
        Assertions.assertFalse(Objects.isNull(result));
    }


    @Test
    void testGetCredentialForClientException() {
        // Setup
        final AbstractCredentialInfo credential = getCredentialDefinition("", "");
        // Verify the run
        Assertions.assertThrows(CredentialsNotFoundException.class,
                () -> testClient.getCredential(credential));
    }


    @Test
    void testGetCesClient() {
        // Setup
        ICredential iCredential =
                testClient.getCredential(getCredentialDefinition("ak", "sk"));

        // Verify the results
        Assertions.assertThrows(Exception.class,
                () -> testClient.getCesClient(iCredential,
                        "eu-west-0"));
    }

    private CredentialVariables getCredentialDefinition(String akValue, String skValue) {

        List<CredentialVariable> credentialVariables = new ArrayList<>();
        CredentialVariable ak = new CredentialVariable(FlexibleEngineMonitorConstants.OS_ACCESS_KEY,
                "The access key.", true, false, akValue);
        credentialVariables.add(ak);
        CredentialVariable sk = new CredentialVariable(FlexibleEngineMonitorConstants.OS_SECRET_KEY,
                "The access key.", true, true, skValue);
        credentialVariables.add(sk);
        return new CredentialVariables(
                Csp.FLEXIBLE_ENGINE, CredentialType.VARIABLES, "AK_SK",
                "The access key and security key.",
                "userId", credentialVariables);
    }
}
