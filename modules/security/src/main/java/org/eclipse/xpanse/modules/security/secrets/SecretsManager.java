/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.secrets;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.SensitiveFieldEncryptionOrDecryptionFailedException;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Manage encryption and decryption of secrets. We can manage secrets using any Java supported
 * security algorithms. See <a
 * href="https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html">Java
 * Doc</a>
 */
@Slf4j
@Component
public class SecretsManager {

    private final byte[] usedSecretKey;
    private final String initialVector;
    private final String algorithmName;
    private final String algorithmMode;
    private final String algorithmPadding;

    /** Constructor for SecretsManager. */
    @Autowired
    public SecretsManager(
            @Value("${xpanse.secrets.encryption.secrete.key.file}") String secretKeyFileName,
            @Value("${xpanse.secrets.encryption.secrete.key.value}") String secretKey,
            @Value("${xpanse.secrets.encryption.initial.vector}") String initialVector,
            @Value("${xpanse.secrets.encryption.algorithm.name}") String algorithmName,
            @Value("${xpanse.secrets.encryption.algorithm.mode}") String algorithmMode,
            @Value("${xpanse.secrets.encryption.algorithm.padding}") String algorithmPadding) {
        byte[] usedSecretKey = getCipherSecretKey(secretKeyFileName, secretKey);
        if (usedSecretKey.length == 0) {
            throw new SensitiveFieldEncryptionOrDecryptionFailedException(
                    "Secret key is empty. Either a secret key or a secret key file is required.");
        }
        this.algorithmName = algorithmName;
        this.algorithmMode = algorithmMode;
        this.algorithmPadding = algorithmPadding;
        this.initialVector = initialVector;
        this.usedSecretKey = usedSecretKey;
        validateConfiguration();
    }

    /** Encrypts the given content using the secret key. */
    public String encrypt(String content) {
        log.debug("encrypting secret content: {}", content);
        try {
            byte[] byteEncode = content.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedContent =
                    getCipher(
                                    Cipher.ENCRYPT_MODE,
                                    usedSecretKey,
                                    initialVector,
                                    algorithmName,
                                    algorithmMode,
                                    algorithmPadding)
                            .doFinal(byteEncode);
            return Base64.encodeBase64String(encryptedContent);
        } catch (Exception e) {
            log.error("Secret encryption error ", e);
            throw new SensitiveFieldEncryptionOrDecryptionFailedException(e.getMessage());
        }
    }

    /** Decrypts the given content using the secret key. */
    public String decrypt(String content) {
        log.debug("Decrypting secret content: {}", content);
        try {
            byte[] byteContent = Base64.decodeBase64(content);
            byte[] byteDecode =
                    getCipher(
                                    Cipher.DECRYPT_MODE,
                                    usedSecretKey,
                                    initialVector,
                                    algorithmName,
                                    algorithmMode,
                                    algorithmPadding)
                            .doFinal(byteContent);
            return new String(byteDecode, StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            log.error("secret decryption error ", e);
            throw new SensitiveFieldEncryptionOrDecryptionFailedException(e.getMessage());
        }
    }

    /**
     * All values are encoded from string. This method decodes and also converts the original string
     * back to its actual type.
     *
     * @param deployVariableDataType type of the data variable that is encoded.
     * @param content encoded string
     * @return returns decoded value and the type converted based on the original data.
     */
    public Object decodeBackToOriginalType(
            DeployVariableDataType deployVariableDataType, String content) {
        String decodedContent = decrypt(content);
        switch (deployVariableDataType) {
            case NUMBER -> {
                return Integer.valueOf(decodedContent);
            }
            case BOOLEAN -> {
                return Boolean.parseBoolean(decodedContent);
            }
            default -> {
                return decodedContent;
            }
        }
    }

    private Cipher getCipher(
            int mode,
            byte[] usedSecretKey,
            String initialVector,
            String algorithmName,
            String algorithmMode,
            String algorithmPadding) {
        Cipher cipher;
        try {
            SecretKey secretKey = new SecretKeySpec(usedSecretKey, algorithmName);
            cipher = Cipher.getInstance(getTransformationFullName());
            cipher.init(
                    mode,
                    secretKey,
                    StringUtils.isNotBlank(initialVector)
                            ? new IvParameterSpec(initialVector.getBytes())
                            : null);
            return cipher;
        } catch (InvalidAlgorithmParameterException
                | NoSuchPaddingException
                | NoSuchAlgorithmException
                | InvalidKeyException e) {
            log.error("get Cipher error ", e);
            throw new SensitiveFieldEncryptionOrDecryptionFailedException(e.getMessage());
        }
    }

    private boolean isSecretKeyAvailableAsFile(String secretKeyFileName) {
        File file = new File(secretKeyFileName);
        return file.exists() || file.length() != 0;
    }

    private byte[] getCipherSecretKey(String secretKeyFileName, String secretKeyValue) {
        if (isSecretKeyAvailableAsFile(secretKeyFileName)) {
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(secretKeyFileName));
                log.info("using secret key found in file {}", secretKeyFileName);
                return bytes;
            } catch (IOException e) {
                log.error("Secret key file found but failed reading content", e);
                throw new SensitiveFieldEncryptionOrDecryptionFailedException(e.getMessage());
            }
        } else {
            log.info(
                    "No secret key file found. Falling back to secret key from configuration"
                            + " parameter instead.");
            return secretKeyValue.getBytes(StandardCharsets.UTF_8);
        }
    }

    private void validateConfiguration() {
        try {
            Cipher.getInstance(getTransformationFullName());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            log.error("Invalid configuration", e);
            throw new SensitiveFieldEncryptionOrDecryptionFailedException(e.getMessage());
        }
    }

    private String getTransformationFullName() {
        return this.algorithmName + '/' + this.algorithmMode + '/' + this.algorithmPadding;
    }
}
