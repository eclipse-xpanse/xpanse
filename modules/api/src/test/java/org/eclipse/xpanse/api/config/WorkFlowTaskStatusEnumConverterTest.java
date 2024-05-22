package org.eclipse.xpanse.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.workflow.WorkFlowTaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkFlowTaskStatusEnumConverterTest {

    private WorkFlowTaskStatusEnumConverter workFlowTaskStatusEnumConverter;

    @BeforeEach
    void setUp() {
        workFlowTaskStatusEnumConverter = new WorkFlowTaskStatusEnumConverter();
    }

    @Test
    void testConvert() {
        assertThat(workFlowTaskStatusEnumConverter.convert("done"))
                .isEqualTo(WorkFlowTaskStatus.DONE);
        assertThat(workFlowTaskStatusEnumConverter.convert("failed"))
                .isEqualTo(WorkFlowTaskStatus.FAILED);
        assertThrows(UnsupportedEnumValueException.class,
                () -> workFlowTaskStatusEnumConverter.convert("unknown"));
    }
}
