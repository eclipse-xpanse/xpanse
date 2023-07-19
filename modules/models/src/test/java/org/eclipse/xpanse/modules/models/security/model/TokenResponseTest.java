package org.eclipse.xpanse.modules.models.security.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenResponseTest {

    private TokenResponse test;

    @BeforeEach
    void setUp() {
        test = new TokenResponse();
        test.setAccessToken("access_token");
        test.setIdToken("id_token");
        test.setExpiresIn("300");
        test.setTokenType("Bearer");
        test.setScopes("openid");
    }


    @Test
    void testGetters() {
        assertEquals("access_token", test.getAccessToken());
        assertEquals("id_token", test.getIdToken());
        assertEquals("300", test.getExpiresIn());
        assertEquals("Bearer", test.getTokenType());
        assertEquals("openid", test.getScopes());
    }


    @Test
    void testEqualsAndHashCode() {

        assertEquals(test, test);
        assertNotEquals(test.hashCode(), 0);

        Object object = new Object();
        assertNotEquals(test, object);
        assertNotEquals(test.hashCode(), object.hashCode());

        TokenResponse test1 = new TokenResponse();
        assertNotEquals(test, test1);
        assertNotEquals(test.hashCode(), test1.hashCode());

        TokenResponse test2 = new TokenResponse();
        TokenResponse test3 = new TokenResponse();
        test2.setAccessToken("access_token");
        test3.setAccessToken("access_token1");
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());

        test2.setIdToken("id_token");
        test2.setIdToken("id_token1");
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());

        test2.setExpiresIn("300");
        test3.setExpiresIn("301");
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());

        test2.setTokenType("Bearer");
        test3.setTokenType("Bearer1");
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());

        test2.setTokenType("openid");
        test3.setTokenType("email");
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());
    }


    @Test
    void testToString() {
        String exceptedString =
                "TokenResponse(accessToken=access_token, tokenType=Bearer, expiresIn=300, idToken=id_token, scopes=openid)";
        assertEquals(test.toString(), exceptedString);
        assertNotEquals(test.toString(), null);
    }

    @Test
    void testToJsonString() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String exceptedString =
                "{\"access_token\":\"access_token\",\"token_type\":\"Bearer\","
                        + "\"expires_in\":\"300\",\"id_token\":\"id_token\",\"scopes\":\"openid\"}";
        assertEquals(mapper.writeValueAsString(test), exceptedString);
    }
}
