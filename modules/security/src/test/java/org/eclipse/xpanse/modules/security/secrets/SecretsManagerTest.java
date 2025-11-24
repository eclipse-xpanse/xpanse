package org.eclipse.xpanse.modules.security.secrets;

import org.eclipse.xpanse.modules.models.common.exceptions.SensitiveFieldEncryptionOrDecryptionFailedException;
import org.eclipse.xpanse.modules.security.config.SecurityProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestPropertySource(
        properties = {
            "xpanse.security.secrets-encryption.initial-vector=p3zV90BqEf3TquKV",
            "xpanse.security.secrets-encryption.algorithm-name=AES",
            "xpanse.security.secrets-encryption.algorithm-mode=CBC",
            "xpanse.security.secrets-encryption.algorithm-padding=ISO10126Padding",
            "xpanse.security.secrets-encryption.secret-key-value=Bx33eHoeifIxykJfMZVPjDRGMKqA75eH",
            "xpanse.security.secrets-encryption.secret-key-file=src/test/resources/aes_sec_test"
        })
@ContextConfiguration(
        classes = {
            SecretsManager.class,
            SecretsManagerTest.class,
            RefreshAutoConfiguration.class,
            SecurityProperties.class
        })
class SecretsManagerTest {

    @Autowired private SecretsManager secretsManagerTest;
    @Autowired private SecurityProperties securityProperties;

    @Test
    void testEncrypt() {
        // SetUp
        String string = "content";

        // Run the test
        final String encodedStr = secretsManagerTest.encrypt(string);

        // Verify the results
        Assertions.assertNotEquals(string, encodedStr);
        Assertions.assertEquals(string, secretsManagerTest.decrypt(encodedStr));
    }

    @Test
    void testDecrypt() {
        // SetUp
        String string = "HelloWorld";

        // Run the test
        final String decodedStr = secretsManagerTest.decrypt(secretsManagerTest.encrypt(string));

        // Verify the results
        Assertions.assertEquals(string, decodedStr);
    }

    @Test
    void testEncryptFailed() {
        // SetUp
        String string = "HelloWorld";

        // Run the test
        final String encodedStr = secretsManagerTest.encrypt(string);

        // Verify the results
        Assertions.assertEquals(string, secretsManagerTest.decrypt(encodedStr));
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void testDecryptThrowsException() {
        // Verify the results
        ReflectionTestUtils.setField(
                securityProperties.getSecretsEncryption(), "secretKeyValue", "");
        ReflectionTestUtils.setField(
                securityProperties.getSecretsEncryption(), "secretKeyFile", "");
        Assertions.assertThrows(
                SensitiveFieldEncryptionOrDecryptionFailedException.class,
                () -> new SecretsManager(securityProperties),
                "Secret key cannot be null or empty");
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void testIncorrectAlgorithmThrowsException() {
        // Verify the results
        ReflectionTestUtils.setField(
                securityProperties.getSecretsEncryption(), "algorithmName", "dummy");
        Assertions.assertThrows(
                SensitiveFieldEncryptionOrDecryptionFailedException.class,
                () -> new SecretsManager(securityProperties),
                "wrong algorithm name will throw exception");
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void testWrongAlgorithmModeThrowsException() {
        ReflectionTestUtils.setField(
                securityProperties.getSecretsEncryption(), "algorithmMode", "dummy");

        Assertions.assertThrows(
                SensitiveFieldEncryptionOrDecryptionFailedException.class,
                () -> new SecretsManager(securityProperties),
                "wrong algorithm mode will throw exception");
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void testWrongAlgorithmPaddingThrowsException() {
        ReflectionTestUtils.setField(
                securityProperties.getSecretsEncryption(), "algorithmPadding", "dummy");

        Assertions.assertThrows(
                SensitiveFieldEncryptionOrDecryptionFailedException.class,
                () -> new SecretsManager(securityProperties),
                "wrong algorithm mode will throw exception");
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void testWrongInitialVectorThrowsException() {
        ReflectionTestUtils.setField(
                securityProperties.getSecretsEncryption(), "initialVector", "");

        Assertions.assertThrows(
                SensitiveFieldEncryptionOrDecryptionFailedException.class,
                () -> new SecretsManager(securityProperties).decrypt(""),
                "wrong algorithm mode will throw exception");
    }
}
