package org.eclipse.xpanse.modules.servicetemplate.utils;

import java.util.List;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.AutoFill;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableDataType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DeployVariableAutoFillValidatorTest {


    @Test
    void testValidateDeployVariableAutoFill() {
        // Setup
        final DeployVariable deployVariable = new DeployVariable();
        deployVariable.setName("name");
        deployVariable.setKind(DeployVariableKind.FIX_ENV);
        deployVariable.setDataType(DeployVariableDataType.STRING);
        deployVariable.setExample("example");
        deployVariable.setMandatory(true);
        final AutoFill autoFill = new AutoFill();
        autoFill.setDeployResourceKind(DeployResourceKind.VPC);
        deployVariable.setAutoFill(autoFill);
        final List<DeployVariable> deployVariables = List.of(deployVariable);

        // Run the test
        Assertions.assertDoesNotThrow(() ->
                DeployVariableAutoFillValidator.validateDeployVariableAutoFill(deployVariables));
    }

    @Test
    void testValidateDeployVariableAutoFillThrowsException() {
        // Setup
        final DeployVariable deployVariable = new DeployVariable();
        deployVariable.setName("name");
        deployVariable.setKind(DeployVariableKind.FIX_ENV);
        deployVariable.setDataType(DeployVariableDataType.STRING);
        deployVariable.setExample("example");
        deployVariable.setMandatory(true);
        final AutoFill autoFill = new AutoFill();
        autoFill.setDeployResourceKind(DeployResourceKind.SUBNET);
        deployVariable.setAutoFill(autoFill);
        final List<DeployVariable> deployVariables = List.of(deployVariable);

        // Run the test
        Assertions.assertThrows(InvalidValueSchemaException.class, () ->
                DeployVariableAutoFillValidator.validateDeployVariableAutoFill(
                        deployVariables));
    }
}
