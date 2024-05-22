package org.eclipse.xpanse.modules.servicetemplate.utils;

import java.util.List;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.AutoFill;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableDataType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DeployVariableSchemaValidatorTest {


    @Test
    void testValidateDeployVariable() {
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
                DeployVariableSchemaValidator.validateDeployVariable(deployVariables));
    }

    @Test
    void testValidateDeployVariableWithInvalidAutoFill() {
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
                DeployVariableSchemaValidator.validateDeployVariable(
                        deployVariables));
    }

    @Test
    void testValidateDeployVariableWithDuplicateNames() {
        // Setup
        final DeployVariable deployVariable = new DeployVariable();
        deployVariable.setName("name");
        deployVariable.setKind(DeployVariableKind.FIX_ENV);
        deployVariable.setDataType(DeployVariableDataType.STRING);
        deployVariable.setExample("example");
        deployVariable.setMandatory(true);
        final DeployVariable deployVariable1 = new DeployVariable();
        deployVariable1.setName("name");
        deployVariable1.setKind(DeployVariableKind.ENV);
        deployVariable1.setDataType(DeployVariableDataType.BOOLEAN);
        deployVariable1.setExample("example1");
        deployVariable1.setMandatory(true);
        final List<DeployVariable> deployVariables = List.of(deployVariable, deployVariable1);

        // Run the test
        Assertions.assertThrows(InvalidValueSchemaException.class, () ->
                DeployVariableSchemaValidator.validateDeployVariable(
                        deployVariables));
    }
}
