package org.eclipse.xpanse.modules.security.secrets;

import org.eclipse.xpanse.modules.models.common.exceptions.SensitiveFieldEncryptionOrDecryptionFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestPropertySource(
        properties = {
            "xpanse.secrets.encryption.initial.vector=p3zV90BqEf3TquKV",
            "xpanse.secrets.encryption.algorithm.name=AES",
            "xpanse.secrets.encryption.algorithm.mode=CBC",
            "xpanse.secrets.encryption.algorithm.padding=ISO10126Padding",
            "xpanse.secrets.encryption.secrete.key.value=Bx33eHoeifIxykJfMZVPjDRGMKqA75eH",
            "xpanse.secrets.encryption.secrete.key.file=src/test/resources/aes_sec_test"
        })
@ContextConfiguration(classes = {SecretsManager.class})
class SecretsManagerTest {

    @Autowired private SecretsManager secretsManagerTest;

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

    @Test
    void testDecryptThrowsException() {
        // Verify the results
        Assertions.assertThrows(
                SensitiveFieldEncryptionOrDecryptionFailedException.class,
                () ->
                        new SecretsManager(
                                "test", "", "p3zV90BqEf3TquKV", "CBC", "AES", "ISO10126Padding"),
                "Secret key cannot be null or empty");

        Assertions.assertThrows(
                SensitiveFieldEncryptionOrDecryptionFailedException.class,
                () ->
                        new SecretsManager(
                                "test", "", "p3zV90BqEf3TquKV", "CBC", "ABC", "ISO10126Padding"),
                "wrong algorithm name will throw exception");

        Assertions.assertThrows(
                SensitiveFieldEncryptionOrDecryptionFailedException.class,
                () ->
                        new SecretsManager(
                                "test",
                                "Bx33eHoeifIxykJfMZVPjDRGMKqA75eH",
                                "p3zV90BqEf3TquKV",
                                "ABC",
                                "AES",
                                "ISO10126Padding"),
                "wrong algorithm mode will throw exception");

        Assertions.assertThrows(
                SensitiveFieldEncryptionOrDecryptionFailedException.class,
                () ->
                        new SecretsManager(
                                "test",
                                "Bx33eHoeifIxykJfMZVPjDRGMKqA75eH",
                                "p3zV90BqEf3TquKV",
                                "CBC",
                                "AES",
                                "ISO10126Paddingabc"),
                "wrong padding mode will throw exception");

        Assertions.assertThrows(
                SensitiveFieldEncryptionOrDecryptionFailedException.class,
                () ->
                        new SecretsManager(
                                "test",
                                "Bx33eHoeifIxykJfMZVPjDRGMKqA75eH",
                                "",
                                "CBC",
                                "AES",
                                "ISO10126Padding"),
                "in correct initial vector value will throw exception");
    }
}
