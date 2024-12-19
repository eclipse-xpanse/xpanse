package org.eclipse.xpanse.modules.security.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class CurrentUserInfoTest {

    @Mock private List<String> mockRoles;
    @Mock private Map<String, String> mockMetadata;

    private CurrentUserInfo test;

    @BeforeEach
    void setUp() {
        test = new CurrentUserInfo();
        test.setUserId("userId");
        test.setUserName("userName");
        test.setRoles(mockRoles);
        test.setMetadata(mockMetadata);
        test.setIsv("isv");
        test.setCsp(Csp.HUAWEI_CLOUD.toValue());
        test.setToken("token");
    }

    @Test
    void testGetters() {
        assertThat(test.getUserId()).isEqualTo("userId");
        assertThat(test.getUserName()).isEqualTo("userName");
        assertThat(test.getRoles()).isEqualTo(mockRoles);
        assertThat(test.getMetadata()).isEqualTo(mockMetadata);
        assertThat(test.getIsv()).isEqualTo("isv");
        assertThat(test.getCsp()).isEqualTo(Csp.HUAWEI_CLOUD.toValue());
        assertThat(test.getToken()).isEqualTo("token");
    }

    @Test
    void testEquals() {
        CurrentUserInfo test1 = new CurrentUserInfo();
        assertThat(test).isNotEqualTo(test1);
        CurrentUserInfo test2 = new CurrentUserInfo();
        BeanUtils.copyProperties(test, test2);
        assertThat(test).isEqualTo(test2);
    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        CurrentUserInfo test1 = new CurrentUserInfo();
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());
        CurrentUserInfo test2 = new CurrentUserInfo();
        BeanUtils.copyProperties(test, test2);
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testToString() {
        String result =
                "CurrentUserInfo(userId=userId, userName=userName, roles=mockRoles,"
                        + " metadata=mockMetadata, isv=isv, csp=HuaweiCloud,"
                        + " token=token)";
        assertThat(test.toString()).isEqualTo(result);
    }
}
