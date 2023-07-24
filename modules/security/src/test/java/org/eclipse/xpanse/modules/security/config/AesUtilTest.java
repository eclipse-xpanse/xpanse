package org.eclipse.xpanse.modules.security.config;

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
        final String encodeString = aesUtilTest.encode(string);

        // Verify the results
        Assertions.assertEquals(string, aesUtilTest.decode(encodeString));
    }


    @Test
    void testDecode() {
        // SetUp
        String string = "HelloWorld";

        // Run the test
        final String decodeString = aesUtilTest.decode(aesUtilTest.encode(string));

        // Verify the results
        Assertions.assertEquals(string, decodeString);
    }

}
