package org.eclipse.xpanse.modules.models.service.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class TaskStatusTest {

    @Test
    void testGetByValue() {
        assertThat(TaskStatus.getByValue("created")).isEqualTo(
                TaskStatus.CREATED);
        assertThat(TaskStatus.getByValue("in progress")).isEqualTo(
                TaskStatus.IN_PROGRESS);
        assertThat(TaskStatus.getByValue("successful")).isEqualTo(
                TaskStatus.SUCCESSFUL);
        assertThat(TaskStatus.getByValue("failed")).isEqualTo(
                TaskStatus.FAILED);
        assertThrows(UnsupportedEnumValueException.class, () ->
                TaskStatus.getByValue("unknown"));
    }

    @Test
    void testToValue() {
        assertThat(TaskStatus.CREATED.toValue()).isEqualTo("created");
        assertThat(TaskStatus.IN_PROGRESS.toValue()).isEqualTo("in progress");
        assertThat(TaskStatus.SUCCESSFUL.toValue()).isEqualTo("successful");
        assertThat(TaskStatus.FAILED.toValue()).isEqualTo("failed");
    }
}
