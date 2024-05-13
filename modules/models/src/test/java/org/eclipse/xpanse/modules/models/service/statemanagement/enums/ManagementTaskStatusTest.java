package org.eclipse.xpanse.modules.models.service.statemanagement.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class ManagementTaskStatusTest {

    @Test
    void testGetByValue() {
        assertThat(ManagementTaskStatus.getByValue("created")).isEqualTo(
                ManagementTaskStatus.CREATED);
        assertThat(ManagementTaskStatus.getByValue("in progress")).isEqualTo(
                ManagementTaskStatus.IN_PROGRESS);
        assertThat(ManagementTaskStatus.getByValue("successful")).isEqualTo(
                ManagementTaskStatus.SUCCESSFUL);
        assertThat(ManagementTaskStatus.getByValue("failed")).isEqualTo(
                ManagementTaskStatus.FAILED);
        assertThrows(UnsupportedEnumValueException.class, () ->
                ManagementTaskStatus.getByValue("unknown"));
    }

    @Test
    void testToValue() {
        assertThat(ManagementTaskStatus.CREATED.toValue()).isEqualTo("created");
        assertThat(ManagementTaskStatus.IN_PROGRESS.toValue()).isEqualTo("in progress");
        assertThat(ManagementTaskStatus.SUCCESSFUL.toValue()).isEqualTo("successful");
        assertThat(ManagementTaskStatus.FAILED.toValue()).isEqualTo("failed");
    }
}
