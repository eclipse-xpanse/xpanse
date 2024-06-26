package org.eclipse.xpanse.modules.security.common;

import org.eclipse.xpanse.modules.models.common.exceptions.SensitiveFieldEncryptionOrDecryptionFailedException;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableDataType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"aes.key.file.name=src/test/resources/aes_sec_test",
        "aes.algorithm.type=AES",
        "aes.key.vi=c558Gq0YQK2QUlMc", "aes.cipher.algorithm=AES/CBC/ISO10126Padding"})
@ContextConfiguration(classes = {AesUtil.class, String.class})
class AesUtilTest {

    @Value("${aes.key.file.name}")
    private String aesKeyFileName;
    @Value("${aes.algorithm.type}")
    private String algorithmType;
    @Value("${aes.key.vi}")
    private String vi;
    @Value("${aes.cipher.algorithm}")
    private String cipherAlgorithm;

    @InjectMocks
    private AesUtil aesUtilTest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aesUtilTest, "aesKeyFileName", aesKeyFileName);
        ReflectionTestUtils.setField(aesUtilTest, "algorithmType", algorithmType);
        ReflectionTestUtils.setField(aesUtilTest, "vi", vi);
        ReflectionTestUtils.setField(aesUtilTest, "cipherAlgorithm", cipherAlgorithm);
    }


    @Test
    void testEncode() {
        // SetUp
        String string = "content";

        // Run the test
        final String encodedStr = aesUtilTest.encode(string);

        // Verify the results
        Assertions.assertNotEquals(string, encodedStr);
        Assertions.assertEquals(string, aesUtilTest.decode(encodedStr));
    }


    @Test
    void testDecode() {
        // SetUp
        String string = "HelloWorld";

        // Run the test
        final String decodedStr = aesUtilTest.decode(aesUtilTest.encode(string));

        // Verify the results
        Assertions.assertEquals(string, decodedStr);
    }

    @Test
    void testEncodeFailed() {
        ReflectionTestUtils.setField(aesUtilTest, "cipherAlgorithm", null);
        // SetUp
        String string = "HelloWorld";

        // Run the test
        final String encodedStr = aesUtilTest.encode(string);

        // Verify the results
        Assertions.assertEquals(string, encodedStr);
    }

    @Test
    void testDecodeThrowsException() {
        // SetUp
        String string = "HelloWorld";
        String encodedStr = aesUtilTest.encode(string);

        ReflectionTestUtils.setField(aesUtilTest, "cipherAlgorithm", "");

        // Verify the results
        Assertions.assertThrows(SensitiveFieldEncryptionOrDecryptionFailedException.class, () ->
                aesUtilTest.decode(encodedStr));
    }

    @Test
    void testEncodeNotEnabled() {
        // SetUp
        String string = "content";
        ReflectionTestUtils.setField(aesUtilTest, "aesKeyFileName",
                "src/test/resources/aes_sec_not_existed");
        // Run the test
        final String encodedStr = aesUtilTest.encode(string);

        // Verify the results
        Assertions.assertEquals(string, encodedStr);
    }

    @Test
    void testDecodeBackToOriginalTypeBoolean() {
        // SetUp
        boolean bool = true;
        // Run the test
        final Object decodedBoolResult = aesUtilTest.decodeBackToOriginalType(
                DeployVariableDataType.BOOLEAN, aesUtilTest.encode(String.valueOf(bool)));
        // Verify the results
        Assertions.assertInstanceOf(Boolean.class, decodedBoolResult);
        Assertions.assertEquals(decodedBoolResult, bool);
    }

    @Test
    void testDecodeBackToOriginalTypeString() {
        // SetUp
        String string = "hello";
        // Run the test
        final Object decodedResult = aesUtilTest.decodeBackToOriginalType(
                DeployVariableDataType.STRING, aesUtilTest.encode(string));
        // Verify the results
        Assertions.assertTrue(decodedResult instanceof String);
        Assertions.assertEquals(decodedResult, string);
    }

    @Test
    void testDecodeBackToOriginalTypeNumber() {
        // SetUp
        int number = 111;
        // Run the test
        final Object decodedResult = aesUtilTest.decodeBackToOriginalType(
                DeployVariableDataType.NUMBER, aesUtilTest.encode(String.valueOf(number)));
        // Verify the results
        Assertions.assertTrue(decodedResult instanceof Integer);
        Assertions.assertEquals((Integer) decodedResult, number);
    }
}
