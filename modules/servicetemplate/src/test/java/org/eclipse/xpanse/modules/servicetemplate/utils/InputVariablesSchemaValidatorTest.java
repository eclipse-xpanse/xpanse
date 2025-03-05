package org.eclipse.xpanse.modules.servicetemplate.utils;

import java.util.List;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.AutoFill;
import org.eclipse.xpanse.modules.models.servicetemplate.InputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.VariableDataType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.VariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InputVariablesSchemaValidatorTest {

    @Test
    void testValidateInputVariables() {
        // Setup
        final InputVariable inputVariable = new InputVariable();
        inputVariable.setName("name");
        inputVariable.setKind(VariableKind.FIX_ENV);
        inputVariable.setDataType(VariableDataType.STRING);
        inputVariable.setExample("example");
        inputVariable.setValue("value");
        inputVariable.setMandatory(true);
        final AutoFill autoFill = new AutoFill();
        autoFill.setDeployResourceKind(DeployResourceKind.VPC);
        inputVariable.setAutoFill(autoFill);
        final List<InputVariable> inputVariables = List.of(inputVariable);

        // Run the test
        Assertions.assertDoesNotThrow(
                () -> InputVariablesSchemaValidator.validateInputVariables(inputVariables));
    }

    @Test
    void testValidateInputVariablesWithInvalidAutoFill() {
        // Setup
        final InputVariable inputVariable = new InputVariable();
        inputVariable.setName("name");
        inputVariable.setKind(VariableKind.FIX_ENV);
        inputVariable.setDataType(VariableDataType.STRING);
        inputVariable.setExample("example");
        inputVariable.setValue("value");
        inputVariable.setMandatory(true);
        final AutoFill autoFill = new AutoFill();
        autoFill.setDeployResourceKind(DeployResourceKind.SUBNET);
        inputVariable.setAutoFill(autoFill);
        final List<InputVariable> inputVariables = List.of(inputVariable);

        // Run the test
        Assertions.assertThrows(
                InvalidValueSchemaException.class,
                () -> InputVariablesSchemaValidator.validateInputVariables(inputVariables));
    }

    @Test
    void testValidateInputVariablesWithDuplicateNames() {
        // Setup
        final InputVariable inputVariable = new InputVariable();
        inputVariable.setName("name");
        inputVariable.setKind(VariableKind.FIX_ENV);
        inputVariable.setDataType(VariableDataType.STRING);
        inputVariable.setExample("example");
        inputVariable.setMandatory(true);
        final InputVariable inputVariable1 = new InputVariable();
        inputVariable1.setName("name");
        inputVariable1.setKind(VariableKind.ENV);
        inputVariable1.setDataType(VariableDataType.BOOLEAN);
        inputVariable1.setExample("example1");
        inputVariable1.setMandatory(true);
        final List<InputVariable> inputVariables = List.of(inputVariable, inputVariable1);

        // Run the test
        Assertions.assertThrows(
                InvalidValueSchemaException.class,
                () -> InputVariablesSchemaValidator.validateInputVariables(inputVariables));
    }
}
