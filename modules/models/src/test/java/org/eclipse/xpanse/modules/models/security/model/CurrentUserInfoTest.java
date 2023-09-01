package org.eclipse.xpanse.modules.models.security.model;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CurrentUserInfoTest {

    private CurrentUserInfo test;

    @BeforeEach
    void setUp() {
        test = new CurrentUserInfo();
        test.setUserId("userId");
        test.setUserName("userName");
        test.setRoles(List.of("admin"));
        test.setMetadata(Map.of("namespace", "test"));
        test.setNamespace("test");
    }

    @Test
    void testGetters() {
        Assertions.assertEquals("userId", test.getUserId());
        Assertions.assertEquals("userName", test.getUserName());
        Assertions.assertEquals("admin", test.getRoles().get(0));
        Assertions.assertTrue(test.getMetadata().containsKey("namespace"));
        Assertions.assertEquals("test", test.getMetadata().get("namespace"));
        Assertions.assertEquals("test", test.getNamespace());
    }


    @Test
    void testEqualsAndHashCode() {

        Assertions.assertEquals(test, test);
        Assertions.assertNotEquals(test.hashCode(), 0);

        Object object = new Object();
        Assertions.assertNotEquals(test, object);
        Assertions.assertNotEquals(test.hashCode(), object.hashCode());

        CurrentUserInfo test1 = new CurrentUserInfo();
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());

        CurrentUserInfo test2 = new CurrentUserInfo();
        CurrentUserInfo test3 = new CurrentUserInfo();
        test2.setUserId("userId2");
        test3.setUserId("userId3");
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test, test3);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test2, test3);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test2.hashCode(), test3.hashCode());

        test2.setUserName("userName2");
        test3.setUserName("userName3");
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test, test3);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test2, test3);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test2.hashCode(), test3.hashCode());

        test2.setRoles(List.of("user"));
        test3.setRoles(List.of("csp"));
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test, test3);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test2, test3);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test2.hashCode(), test3.hashCode());

        test2.setMetadata(Map.of("namespace", "test"));
        test3.setMetadata(Map.of("namespace", "test1"));
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test, test3);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test2, test3);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test2.hashCode(), test3.hashCode());

        test2.setNamespace("test");
        test3.setNamespace("test1");
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test, test3);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test2, test3);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test2.hashCode(), test3.hashCode());
    }


    @Test
    void testToString() {
        String exceptedString = "CurrentUserInfo(userId=userId, userName=userName, roles=[admin],"
                + " metadata={namespace=test}, namespace=test)";
        Assertions.assertEquals(test.toString(), exceptedString);
        Assertions.assertNotEquals(test.toString(), null);
    }
}
