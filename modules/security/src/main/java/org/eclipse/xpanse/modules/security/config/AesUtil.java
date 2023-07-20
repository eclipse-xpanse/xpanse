/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.xpanse.modules.models.common.exceptions.SensitiveFieldEncryptionOrDecryptionFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AES encryption tool class.
 */
@Slf4j
@Component
public class AesUtil {

    @Value("${aes.key.file.name}")
    private String aesKeyFileName;
    @Value("${aes.algorithm.type}")
    private String algorithmType;
    @Value("${aes.key.vi}")
    private String vi;
    @Value("${aes.cipher.algorithm}")
    private String cipherAlgorithm;

    /**
     * AES encryption.
     */
    public String encode(String content) {
        try {
            if (!isEnabledAes()) {
                return content;
            }
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
            if (Objects.isNull(cipher)) {
                log.error("AES encode error, Get Cipher failed.");
                throw new SensitiveFieldEncryptionOrDecryptionFailedException(
                        "AES encode error, Get Cipher failed.");
            }
            byte[] byteEncode = content.getBytes(StandardCharsets.UTF_8);
            byte[] byteAes = cipher.doFinal(byteEncode);
            return Base64.encodeBase64String(byteAes);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("AES encode error, {}", e.getMessage());
        }
        return content;
    }

    /**
     * AES decryption.
     */
    public String decode(String content) {
        try {
            if (!isEnabledAes()) {
                return content;
            }
            Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
            if (Objects.isNull(cipher)) {
                log.error("AES decode error, Get Cipher failed.");
                throw new SensitiveFieldEncryptionOrDecryptionFailedException(
                        "AES decode error, Get Cipher failed.");
            }
            byte[] byteContent = Base64.decodeBase64(content);
            byte[] byteDecode = cipher.doFinal(byteContent);
            return new String(byteDecode, StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            log.error("AES decode error, {}", e.getMessage());
        }
        return content;
    }

    private Cipher getCipher(int mode) {
        Cipher cipher = null;
        try (InputStream is = new FileInputStream(
                System.getProperty("user.dir") + File.separator + aesKeyFileName)) {
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            SecretKey secretKey = new SecretKeySpec(bytes, algorithmType);
            cipher = Cipher.getInstance(cipherAlgorithm);
            cipher.init(mode, secretKey, new IvParameterSpec(vi.getBytes()));
            return cipher;
        } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            log.error("get Cipher error, {}", e.getMessage());
        }
        return null;
    }

    /**
     * Whether to enable AES encryption and decryption.
     */
    private boolean isEnabledAes() {
        File file = new File(System.getProperty("user.dir")
                + File.separator + aesKeyFileName);
        if (file.exists() && file.length() != 0) {
            return true;
        }
        return false;
    }

}
