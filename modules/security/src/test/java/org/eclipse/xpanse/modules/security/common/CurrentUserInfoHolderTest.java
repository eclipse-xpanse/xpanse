package org.eclipse.xpanse.modules.security.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CurrentUserInfoHolderTest {

    @Test
    @Order(1)
    void testSetCurrentUserInfo() {
        // Setup
        final CurrentUserInfo currentUserInfo = new CurrentUserInfo();
        currentUserInfo.setUserId("userId");
        currentUserInfo.setUserName("userName");
        currentUserInfo.setRoles(List.of("value"));
        currentUserInfo.setMetadata(Map.ofEntries(Map.entry("csp", "huawei")));
        currentUserInfo.setToken("token");

        // Run the test
        CurrentUserInfoHolder.setCurrentUserInfo(currentUserInfo);

        // Verify the results
    }

    @Test
    @Order(2)
    void testGetCurrentUserInfo() {
        // Setup
        final CurrentUserInfo expectedResult = new CurrentUserInfo();
        expectedResult.setUserId("userId");
        expectedResult.setUserName("userName");
        expectedResult.setRoles(List.of("value"));
        expectedResult.setMetadata(Map.ofEntries(Map.entry("csp", "huawei")));
        expectedResult.setToken("token");

        // Run the test
        final CurrentUserInfo result = CurrentUserInfoHolder.getCurrentUserInfo();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    @Order(3)
    void testGetToken() {
        assertThat(CurrentUserInfoHolder.getToken()).isEqualTo("token");
    }

    @Test
    @Order(4)
    void testClear() {
        // Setup
        // Run the test
        CurrentUserInfoHolder.clear();
        // Verify the results
        assertThat(CurrentUserInfoHolder.getCurrentUserInfo()).isNull();
        assertThat(CurrentUserInfoHolder.getToken()).isNull();
    }


}
