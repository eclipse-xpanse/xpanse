package org.eclipse.xpanse.runtime.modules.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.Operation;
import java.util.List;
import org.eclipse.xpanse.api.controllers.AdminServicesApi;
import org.eclipse.xpanse.modules.security.zitadel.config.RequiredRoleDescriptionCustomizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.method.HandlerMethod;

@ExtendWith(SpringExtension.class)
class RequiredRoleDescriptionCustomizerTest {

    private RequiredRoleDescriptionCustomizer requiredRoleDescriptionCustomizerUnderTest;

    @BeforeEach
    void setUp() {
        requiredRoleDescriptionCustomizerUnderTest = new RequiredRoleDescriptionCustomizer();
    }

    @Test
    void testCustomize() throws Exception {
        // Setup
        final Operation operation = new Operation();
        operation.tags(List.of("value"));
        operation.summary("summary");
        operation.setDescription("description");
        final ExternalDocumentation externalDocs = new ExternalDocumentation();
        externalDocs.description("description");
        operation.externalDocs(externalDocs);

        final HandlerMethod handlerMethod =
                new HandlerMethod(new AdminServicesApi(), "healthCheck", null);
        final Operation expectedResult = new Operation();
        expectedResult.tags(List.of("value"));
        expectedResult.summary("summary");
        expectedResult.setDescription(
                "description<br>Required role:<b> admin</b> or <b>isv</b> or <b>user</b>");
        final ExternalDocumentation externalDocs1 = new ExternalDocumentation();
        externalDocs1.description("description");
        expectedResult.externalDocs(externalDocs1);

        // Run the test
        final Operation result =
                requiredRoleDescriptionCustomizerUnderTest.customize(operation, handlerMethod);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
}
