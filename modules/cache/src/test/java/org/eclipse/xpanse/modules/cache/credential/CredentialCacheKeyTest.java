package org.eclipse.xpanse.modules.cache.credential;

import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CredentialCacheKeyTest {

    private final CredentialCacheKey testCacheKey = new CredentialCacheKey(Csp.HUAWEI_CLOUD,
            "Chinese Mainland", CredentialType.VARIABLES, "AK_SK", "userId");

    @Test
    void testEqualsAndHashCode() {
        final Object object = new Object();
        Assertions.assertNotEquals(testCacheKey, object);
        Assertions.assertNotEquals(testCacheKey.hashCode(), object.hashCode());

        final CredentialCacheKey cacheKey1 =
                new CredentialCacheKey(null, null, null, null, null);
        Assertions.assertNotEquals(testCacheKey, cacheKey1);
        Assertions.assertNotEquals(testCacheKey.hashCode(), cacheKey1.hashCode());

        final CredentialCacheKey cacheKey2 = new CredentialCacheKey(Csp.AWS, null,
                CredentialType.VARIABLES, "AK_SK", "userId");
        Assertions.assertNotEquals(testCacheKey, cacheKey2);
        Assertions.assertNotEquals(testCacheKey.hashCode(), cacheKey2.hashCode());

        final CredentialCacheKey cacheKey3 = new CredentialCacheKey(Csp.HUAWEI_CLOUD, null,
                CredentialType.API_KEY, "AK_SK", "userId");
        Assertions.assertNotEquals(testCacheKey, cacheKey3);
        Assertions.assertNotEquals(testCacheKey.hashCode(), cacheKey3.hashCode());

        final CredentialCacheKey cacheKey4 = new CredentialCacheKey(Csp.HUAWEI_CLOUD, null,
                CredentialType.VARIABLES, "AK_SK1", "userId");
        Assertions.assertNotEquals(testCacheKey, cacheKey4);
        Assertions.assertNotEquals(testCacheKey.hashCode(), cacheKey4.hashCode());

        final CredentialCacheKey cacheKey5 = new CredentialCacheKey(Csp.HUAWEI_CLOUD, null,
                CredentialType.VARIABLES, "AK_SK", "userId1");
        Assertions.assertNotEquals(testCacheKey, cacheKey5);
        Assertions.assertNotEquals(testCacheKey.hashCode(), cacheKey5.hashCode());

        final CredentialCacheKey cacheKey6 = new CredentialCacheKey(Csp.HUAWEI_CLOUD,
                "Chinese Mainland", CredentialType.VARIABLES, "AK_SK", "userId");
        Assertions.assertEquals(testCacheKey, cacheKey6);
        Assertions.assertEquals(testCacheKey.hashCode(), cacheKey6.hashCode());
    }

    @Test
    void testToString() {
        String result = "CredentialCacheKey[csp=HUAWEI_CLOUD, site=Chinese Mainland, "
                + "credentialType=VARIABLES, credentialName=AK_SK, userId=userId]";
        Assertions.assertEquals(result, testCacheKey.toString());
    }

}
