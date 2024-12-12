package org.eclipse.xpanse.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.junit.jupiter.api.Test;

class TaskStatusEnumConverterTest {

    private final TaskStatusEnumConverter converterTest = new TaskStatusEnumConverter();

    @Test
    void testConvert() {
        assertThat(converterTest.convert("created")).isEqualTo(TaskStatus.CREATED);
        assertThat(converterTest.convert("in-progress")).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(converterTest.convert("successful")).isEqualTo(TaskStatus.SUCCESSFUL);
        assertThat(converterTest.convert("failed")).isEqualTo(TaskStatus.FAILED);
        assertThrows(UnsupportedEnumValueException.class, () -> converterTest.convert("unknown"));
    }
}
