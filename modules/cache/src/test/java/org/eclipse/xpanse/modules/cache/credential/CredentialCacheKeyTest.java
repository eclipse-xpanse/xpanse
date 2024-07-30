package org.eclipse.xpanse.modules.cache.credential;

import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CredentialCacheKeyTest {

    private final CredentialCacheKey testCacheKey = new CredentialCacheKey(Csp.HUAWEI_CLOUD,
            CredentialType.VARIABLES, "resourceId", "userId");

    @Test
    void testEqualsAndHashCode() {
        final Object object = new Object();
        Assertions.assertNotEquals(testCacheKey, object);
        Assertions.assertNotEquals(testCacheKey.hashCode(), object.hashCode());

        final CredentialCacheKey cacheKey1 =
                new CredentialCacheKey(null, null, null, null);
        Assertions.assertNotEquals(testCacheKey, cacheKey1);
        Assertions.assertNotEquals(testCacheKey.hashCode(), cacheKey1.hashCode());

        final CredentialCacheKey cacheKey2 = new CredentialCacheKey(Csp.AWS,
                CredentialType.VARIABLES, "resourceId", "userId");
        Assertions.assertNotEquals(testCacheKey, cacheKey2);
        Assertions.assertNotEquals(testCacheKey.hashCode(), cacheKey2.hashCode());

        final CredentialCacheKey cacheKey3 = new CredentialCacheKey(Csp.HUAWEI_CLOUD,
                CredentialType.API_KEY, "resourceId", "userId");
        Assertions.assertNotEquals(testCacheKey, cacheKey3);
        Assertions.assertNotEquals(testCacheKey.hashCode(), cacheKey3.hashCode());

        final CredentialCacheKey cacheKey4 = new CredentialCacheKey(Csp.HUAWEI_CLOUD,
                CredentialType.VARIABLES, "resourceId1", "userId");
        Assertions.assertNotEquals(testCacheKey, cacheKey4);
        Assertions.assertNotEquals(testCacheKey.hashCode(), cacheKey4.hashCode());

        final CredentialCacheKey cacheKey5 = new CredentialCacheKey(Csp.HUAWEI_CLOUD,
                CredentialType.VARIABLES, "resourceId", "userId1");
        Assertions.assertNotEquals(testCacheKey, cacheKey5);
        Assertions.assertNotEquals(testCacheKey.hashCode(), cacheKey5.hashCode());

        final CredentialCacheKey cacheKey6 = new CredentialCacheKey(Csp.HUAWEI_CLOUD,
                CredentialType.VARIABLES, "resourceId", "userId");
        Assertions.assertEquals(testCacheKey, cacheKey6);
        Assertions.assertEquals(testCacheKey.hashCode(), cacheKey6.hashCode());
    }

    @Test
    void testToString() {
        String result = "CredentialCacheKey[csp=HUAWEI_CLOUD, credentialType=VARIABLES, "
                + "credentialName=resourceId, userId=userId]";
        Assertions.assertEquals(result, testCacheKey.toString());
    }

}
