package org.eclipse.xpanse.modules.database.userpolicy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class UserPolicyEntityTest {

    private final Boolean enabled = true;
    private final String policy = "policy";
    private final UUID id = UUID.fromString("8f06ca43-e699-424e-92d4-37b85f35134f");
    private final String userId = "userId";
    @Mock
    private Csp mockCsp;
    private UserPolicyEntity test;

    @BeforeEach
    void setUp() {
        test = new UserPolicyEntity();
        test.setCsp(mockCsp);
        test.setEnabled(enabled);
        test.setPolicy(policy);
        test.setId(id);
        test.setUserId(userId);
    }

    @Test
    void testGetters() {
        assertThat(test.getId()).isEqualTo(id);
        assertThat(test.getPolicy()).isEqualTo(policy);
        assertThat(test.getCsp()).isEqualTo(mockCsp);
        assertThat(test.getEnabled()).isEqualTo(enabled);
        assertThat(test.getUserId()).isEqualTo(userId);
    }

    @Test
    void testCreateTimeGetterAndSetter() {
        final OffsetDateTime createTime =
                OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0), ZoneOffset.UTC);
        test.setCreateTime(createTime);
        assertThat(test.getCreateTime()).isEqualTo(createTime);
    }

    @Test
    void testLastModifiedTimeGetterAndSetter() {
        final OffsetDateTime lastModifiedTime =
                OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0), ZoneOffset.UTC);
        test.setLastModifiedTime(lastModifiedTime);
        assertThat(test.getLastModifiedTime()).isEqualTo(lastModifiedTime);
    }

    @Test
    void testEquals() {
        assertThat(test.equals(new Object())).isFalse();
        UserPolicyEntity test1 = new UserPolicyEntity();
        BeanUtils.copyProperties(test, test1);
        assertThat(test.equals(test1)).isTrue();
    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        UserPolicyEntity test1 = new UserPolicyEntity();
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());
        UserPolicyEntity test2 = new UserPolicyEntity();
        BeanUtils.copyProperties(test, test2);
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testToString() {
        String result = String.format("UserPolicyEntity(id=%s, userId=%s, policy=%s, csp=%s, enabled=%s)",
                id, userId, policy, mockCsp, enabled);
        assertThat(test.toString()).isEqualTo(result);
    }
}
