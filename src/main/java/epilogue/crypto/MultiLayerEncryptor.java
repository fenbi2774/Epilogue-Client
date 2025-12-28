package epilogue.crypto;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import java.util.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class MultiLayerEncryptor {
    // 直接使用字符串密钥，不使用数组
    private static String desKey;
    private static String aesKey;
    private static String rc4Key;
    private static String blowfishKey;
    private static String xorKey;
    private static String customKey;
    // ========== 新增：RSA/SM2 Base64 密钥字符串 ==========
    private static String rsaPublicKeyB64;
    private static String rsaPrivateKeyB64;
    private static String sm2PublicKeyB64;
    private static String sm2PrivateKeyB64;

    // ========== 运行时解析后的密钥对象 ==========
    private static PublicKey rsaPublicKey;
    private static PrivateKey rsaPrivateKey;
    private static PublicKey sm2PublicKey;
    private static PrivateKey sm2PrivateKey;

    // 初始化默认密钥
    static {
        setKeys(
                "9MaR<j?w",                    // DES
                "_l1B8s8dXvyS3V#t7kO}NKb)Vo.UF_*d",  // AES
                "2UL@Lu}v$D5unp68",           // RC4
                "jl.-vXQ3?LLk_q8Pkyt)_7&W",   // Blowfish
                "VikO_WQ1cmsXlEEXW}{U",       // XOR
                "[R(2YiX]zuHQe$38",           // Custom
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhxNTh311q+un2xEbnJcuj8oUF+U1y8uObj2awZyKXIxEENIz/v5EbsY6Kdzin45Qf/wArmwaw2p4Q6mkDfw7MDbNNORXCykNC32DnO00a1T9KbY7dMa6JN3wdeRPDa+uCe6w38Wy+8vW231doFdSyC050PwQIDHPvDD2X4DscGcTBsEQ7SSu+WTGCeZktTi62aqkpTKi6EqH3h9/9uteU4rUM3vU78DDbVG2dIZFoCTkK9DQsPStg+BDmLzHzf+wm0DU+Xoeilzing2/5GhIRsF7M576JeB2eK4r5kvxmdrKKWGE/d9xiqmkAIWl3ttiB38fh6FxCHlygsZgqP5TDwIDAQAB",                            // RSA Public Key (Base64)
                "MIIEowIBAAKCAQEAhxNTh311q+un2xEbnJcuj8oUF+U1y8uObj2awZyKXIxEENIz/v5EbsY6Kdzin45Qf/wArmwaw2p4Q6mkDfw7MDbNNORXCykNC32DnO00a1T9KbY7dMa6JN3wdeRPDa+uCe6w38Wy+8vW231doFdSyC050PwQIDHPvDD2X4DscGcTBsEQ7SSu+WTGCeZktTi62aqkpTKi6EqH3h9/9uteU4rUM3vU78DDbVG2dIZFoCTkK9DQsPStg+BDmLzHzf+wm0DU+Xoeilzing2/5GhIRsF7M576JeB2eK4r5kvxmdrKKWGE/d9xiqmkAIWl3ttiB38fh6FxCHlygsZgqP5TDwIDAQABAoIBAF8Up5OUKKSf+UQnQrxzXv8wS/yWB8wwuU24Z5spyetjgPYmQRuZeCpRtteI+K0/zEKK7R/aUOyFoapfW2/MwQ0rRj+ZC/x7JQeVjnOENYXoCVy3PhrxWE5jO8SAtXT42RV8w1yBGYBM+C0AjkszrL3jt/L3u5It/gCGpqaLZeOoWzvOz/ctUzLIs5OWztI/y2oInAcmmYbMmAgpl0o5WG1waZIx6QRtVG0bRk8yz2W63GSEMUUwaqZKsj+COY3PULqaAPsUenw2usXIoI/U3LAyYSPi93tLls8eHUKNZwxo6kM6LIqUoEKM44hVdtfAiW/YFJzKNzLmji/Ctmh5UQECgYEA87EbMRnetobynpnv9WMrhF7QjHh9AWDfp7YFiS2tmv9p2ctPkDdPrNFX8DEjC+UOJnYwrmp4ZiXY/kHdnYkJeWnTJQpCSZM31VeEO2g1muCEI0olSfsK1/jtibSM1WzTNM18nMb5OrAFivuFpAcg98FAZjf5CMh9Vf7P3oj/U28CgYEAjeXUVZH7+WD0gUSIH6vTHROAc2gHKE1tPCjkOknqLQnO2qaHFNmzyT0VnOsabTP7+09h0Qaow0dyR8p+g0H12Kq4idaMhxJaCsle4sxjcQ/DvWg4efMDgt7oGRnTTPSJu5qZSFih64V8fIXOAGSjqAwJHXtv6l0TWIYWv66oqmECgYBQxzhYQlcM8haidCySjtjx++vtZFMrjc0VbsTPABE3+8o+o6IwD+WdR5d6yw7u9nKGVU81wZ0/XvRa88JeYfp+AJI5CPmIIgCz+1qdxqFeQWYLvJw3tbuWc7FpoCu+41/vdN4Fqf8bcRWlSFK4WJSZC7opKAyo2KyTQO/uuELKiQKBgHBlNs5c5vC+ao2mAgjMKlnio6FGKj0zLy4y2ayN9a6dETtCIsdSNcVV3DPCSdlsDtEeLMXL94tOoWfaLmG7sodfmvKbfg5Ta/0VSlJtQOtCaHI0BmAaLJLSC5guS5+uJxbxuy6B/ie3QUbFGcpX7QXkGqg+qQRDm1pIQ+aq6zChAoGBALVEk9iIPXurNTVwZKArbQQeuP/Metk2M/IkvUSfvZYfAM8RfmIQEWzmKhFEvWIwudNVIii2bdc64iri4fD+zcNm4c+JjkQocFEwGG23pvPvJcud2eDwao+9dqGnajq5H4E8uwS6TOF5mFKc7rqqgVhuBvPgL0VlJvqTs4+915BT",                            // RSA Private Key (Base64)
                "MDQ0ZDE0MDAzNDA5ZTdmMjhmYTE4MzQ3MzM2YThmM2E0YjUyZWRlYTViYzQ0YWU3MDk2ODc3Nzc3ZTk5YmNhNDM3ZmJkODg4ZWE1YTYyODk4MGEyOGI0NzhkNjdkZGI2ZjI4NzBmZGM3MmQxOTY3MDIzM2IyZTY0NWFmYWMwYzE0Mg==",                            // SM2 Public Key (Base64)
                "Y2RhNzE5ZjA1NzExNWRlOWUyYWIzMmY5YWYwMzE2M2ZmNjY1NzMyODU4Yjc0NTc3YmFiMTlkOTM0ZmE0MGZiYw=="                             // SM2 Private Key (Base64)
        );
    }

    // 设置所有密钥（含 RSA/SM2 的 Base64 字符串）
    public static void setKeys(String des, String aes, String rc4,
                               String blowfish,
                               String xor, String custom,
                               String rsaPub, String rsaPri,
                               String sm2Pub, String sm2Pri) {
        // 确保密钥达到所需长度
        desKey = padKey(des, 8);
        aesKey = padKey(aes, 32);
        rc4Key = padKey(rc4, 16);
        blowfishKey = padKey(blowfish, 24);
        xorKey = (xor == null || xor.isEmpty()) ? "default_xor_key_123" : xor;
        customKey = padKey(custom, 16);

        // 保存原始 Base64 字符串
        rsaPublicKeyB64 = rsaPub != null ? rsaPub : "";
        rsaPrivateKeyB64 = rsaPri != null ? rsaPri : "";
        sm2PublicKeyB64 = sm2Pub != null ? sm2Pub : "";
        sm2PrivateKeyB64 = sm2Pri != null ? sm2Pri : "";

        // 解析 RSA 密钥
        try {
            if (!rsaPublicKeyB64.isEmpty() && !rsaPrivateKeyB64.isEmpty()) {
                KeyFactory kf = KeyFactory.getInstance("RSA");
                rsaPublicKey = kf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(rsaPublicKeyB64)));
                rsaPrivateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(rsaPrivateKeyB64)));
            } else {
                rsaPublicKey = null;
                rsaPrivateKey = null;
            }
        } catch (Exception e) {
            System.err.println("[RSA] Key parse failed: " + e.getMessage());
            rsaPublicKey = null;
            rsaPrivateKey = null;
        }

        // 解析 SM2 密钥
        try {
            if (!sm2PublicKeyB64.isEmpty() && !sm2PrivateKeyB64.isEmpty()) {
                // 确保 Bouncy Castle 已注册
                if (Security.getProvider("BC") == null) {
                    Security.addProvider(new BouncyCastleProvider());
                }
                KeyFactory kf = KeyFactory.getInstance("EC", "BC");
                sm2PublicKey = kf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(sm2PublicKeyB64)));
                sm2PrivateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(sm2PrivateKeyB64)));
            } else {
                sm2PublicKey = null;
                sm2PrivateKey = null;
            }
        } catch (Exception e) {
            System.err.println("[SM2] Key parse failed: " + e.getMessage());
            sm2PublicKey = null;
            sm2PrivateKey = null;
        }
    }

    // 填充密钥到指定长度
    private static String padKey(String key, int length) {
        if (key == null || key.isEmpty()) {
            key = "default_key_placeholder";
        }

        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length == length) {
            return key;
        } else if (keyBytes.length > length) {
            // 截断
            return new String(Arrays.copyOf(keyBytes, length), StandardCharsets.UTF_8);
        } else {
            // 循环填充
            byte[] padded = new byte[length];
            for (int i = 0; i < length; i++) {
                padded[i] = keyBytes[i % keyBytes.length];
            }
            return new String(padded, StandardCharsets.UTF_8);
        }
    }

    // ======== 加密方法 ========

    private static byte[] desEncrypt(byte[] data) throws Exception {
        byte[] keyBytes = desKey.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        SecretKeySpec spec = new SecretKeySpec(keyBytes, "DES");
        cipher.init(Cipher.ENCRYPT_MODE, spec);
        return cipher.doFinal(data);
    }

    private static byte[] desDecrypt(byte[] data) throws Exception {
        byte[] keyBytes = desKey.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        SecretKeySpec spec = new SecretKeySpec(keyBytes, "DES");
        cipher.init(Cipher.DECRYPT_MODE, spec);
        return cipher.doFinal(data);
    }

    private static byte[] aesEncrypt(byte[] data) throws Exception {
        byte[] keyBytes = aesKey.getBytes(StandardCharsets.UTF_8);
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec spec = new SecretKeySpec(keyBytes, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, spec, new IvParameterSpec(iv));

        byte[] encrypted = cipher.doFinal(data);

        // 合并 IV + 密文
        byte[] result = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
        return result;
    }

    private static byte[] aesDecrypt(byte[] data) throws Exception {
        // 检查数据长度
        if (data.length < 16) {
            throw new IllegalArgumentException("AES data too short");
        }

        byte[] keyBytes = aesKey.getBytes(StandardCharsets.UTF_8);
        byte[] iv = Arrays.copyOfRange(data, 0, 16);
        byte[] encrypted = Arrays.copyOfRange(data, 16, data.length);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec spec = new SecretKeySpec(keyBytes, "AES");
        cipher.init(Cipher.DECRYPT_MODE, spec, new IvParameterSpec(iv));

        return cipher.doFinal(encrypted);
    }

    private static byte[] rc4Encrypt(byte[] data) throws Exception {
        byte[] keyBytes = rc4Key.getBytes(StandardCharsets.UTF_8);

        // RC4初始化
        byte[] s = new byte[256];
        for (int i = 0; i < 256; i++) {
            s[i] = (byte) i;
        }

        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + (s[i] & 0xFF) + (keyBytes[i % keyBytes.length] & 0xFF)) & 0xFF;
            byte temp = s[i];
            s[i] = s[j];
            s[j] = temp;
        }

        // RC4加密
        byte[] output = new byte[data.length];
        int i = 0;
        j = 0;

        for (int k = 0; k < data.length; k++) {
            i = (i + 1) & 0xFF;
            j = (j + (s[i] & 0xFF)) & 0xFF;

            byte temp = s[i];
            s[i] = s[j];
            s[j] = temp;

            int t = ((s[i] & 0xFF) + (s[j] & 0xFF)) & 0xFF;
            output[k] = (byte) (data[k] ^ s[t]);
        }

        return output;
    }

    // RC4解密与加密相同
    private static byte[] rc4Decrypt(byte[] data) throws Exception {
        return rc4Encrypt(data);
    }

    private static byte[] blowfishEncrypt(byte[] data) throws Exception {
        byte[] keyBytes = blowfishKey.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
        SecretKeySpec spec = new SecretKeySpec(keyBytes, "Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, spec);
        return cipher.doFinal(data);
    }

    private static byte[] blowfishDecrypt(byte[] data) throws Exception {
        byte[] keyBytes = blowfishKey.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
        SecretKeySpec spec = new SecretKeySpec(keyBytes, "Blowfish");
        cipher.init(Cipher.DECRYPT_MODE, spec);
        return cipher.doFinal(data);
    }

    private static byte[] xorEncrypt(byte[] data) {
        byte[] keyBytes = xorKey.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ keyBytes[i % keyBytes.length]);
        }
        return result;
    }

    // XOR解密与加密相同
    private static byte[] xorDecrypt(byte[] data) {
        return xorEncrypt(data);
    }

    private static byte[] customEncrypt(byte[] data) {
        byte[] keyBytes = customKey.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) ((data[i] + keyBytes[i % keyBytes.length]) & 0xFF);
        }
        return result;
    }

    private static byte[] customDecrypt(byte[] data) {
        byte[] keyBytes = customKey.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) ((data[i] - keyBytes[i % keyBytes.length]) & 0xFF);
        }
        return result;
    }

    // ======== RSA / SM2 加解密方法 ========

    private static byte[] rsaEncrypt(byte[] data) throws Exception {
        if (rsaPublicKey == null) throw new IllegalStateException("RSA public key not set");
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
        return cipher.doFinal(data);
    }

    private static byte[] rsaDecrypt(byte[] data) throws Exception {
        if (rsaPrivateKey == null) throw new IllegalStateException("RSA private key not set");
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
        return cipher.doFinal(data);
    }

    private static byte[] sm2Encrypt(byte[] data) throws Exception {
        if (sm2PublicKey == null) throw new IllegalStateException("SM2 public key not set");
        Cipher cipher = Cipher.getInstance("SM2", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, sm2PublicKey);
        return cipher.doFinal(data);
    }

    private static byte[] sm2Decrypt(byte[] data) throws Exception {
        if (sm2PrivateKey == null) throw new IllegalStateException("SM2 private key not set");
        Cipher cipher = Cipher.getInstance("SM2", "BC");
        cipher.init(Cipher.DECRYPT_MODE, sm2PrivateKey);
        return cipher.doFinal(data);
    }

    private static byte[] base32Encode(byte[] data) {
        if (data == null || data.length == 0) {
            return new byte[0];
        }
        return Base32.getEncoder().encode(data);
    }

    private static byte[] base32Decode(byte[] data) {
        if (data == null || data.length == 0) {
            return new byte[0];
        }
        return Base32.getDecoder().decode(data);
    }

    private static byte[] base64Encode(byte[] data) {
        if (data == null || data.length == 0) {
            return new byte[0];
        }
        return Base64.getEncoder().encode(data);
    }

    private static byte[] base64Decode(byte[] data) {
        if (data == null || data.length == 0) {
            return new byte[0];
        }
        return Base64.getDecoder().decode(data);
    }

    // ======== 主加密方法 ========

    /**
     * 多层加密（可选择使用RSA/SM2）
     * @param plainText 明文
     * @param useRsa 是否使用RSA加密
     * @param useSm2 是否使用SM2加密
     * @return 加密后的Base64字符串
     */
    public static String encrypt(String plainText, boolean useRsa, boolean useSm2) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            return "";
        }

        byte[] data = plainText.getBytes(StandardCharsets.UTF_8);

        try {
            // 加密流程
            data = desEncrypt(data);
            data = aesEncrypt(data);
            data = rc4Encrypt(data);
            data = blowfishEncrypt(data);
            data = xorEncrypt(data);
            data = customEncrypt(data);

            // ========== 新增：RSA/SM2 非对称加密 ==========
            if (useRsa) {
                if (rsaPublicKey == null) throw new IllegalStateException("RSA public key not set");
                data = rsaEncrypt(data);
            }
            if (useSm2) {
                if (sm2PublicKey == null) throw new IllegalStateException("SM2 public key not set");
                data = sm2Encrypt(data);
            }
            // ==========================================

            data = base32Encode(data);
            data = base64Encode(data);

            return new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new Exception("Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * 多层加密（默认不使用RSA/SM2，保持向后兼容）
     */
    public static String encrypt(String plainText) throws Exception {
        return encrypt(plainText, false, false);
    }

    /**
     * 多层解密（可选择使用RSA/SM2）
     * @param encryptedText 加密文本
     * @param useRsa 是否使用RSA解密
     * @param useSm2 是否使用SM2解密
     * @return 解密后的明文
     */
    public static String decrypt(String encryptedText, boolean useRsa, boolean useSm2) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return "";
        }

        byte[] data = encryptedText.getBytes(StandardCharsets.UTF_8);

        try {
            // 解密流程（完全反向）
            data = base64Decode(data);
            data = base32Decode(data);

            // ========== 新增：RSA/SM2 非对称解密（顺序与加密相反）==========
            if (useSm2) {
                if (sm2PrivateKey == null) throw new IllegalStateException("SM2 private key not set");
                data = sm2Decrypt(data);
            }
            if (useRsa) {
                if (rsaPrivateKey == null) throw new IllegalStateException("RSA private key not set");
                data = rsaDecrypt(data);
            }
            // ==========================================

            data = customDecrypt(data);
            data = xorDecrypt(data);
            data = blowfishDecrypt(data);
            data = rc4Decrypt(data);
            data = aesDecrypt(data);
            data = desDecrypt(data);

            return new String(data, StandardCharsets.UTF_8);
        } catch (BadPaddingException e) {
            // 特殊处理填充错误
            throw new Exception("Decryption failed: Bad padding (wrong key or corrupted data)", e);
        } catch (Exception e) {
            throw new Exception("Decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * 多层解密（默认不使用RSA/SM2，保持向后兼容）
     */
    public static String decrypt(String encryptedText) throws Exception {
        return decrypt(encryptedText, false, false);
    }

    // ======== 密钥状态检查 ========

    public static boolean isRsaKeysAvailable() {
        return rsaPublicKey != null && rsaPrivateKey != null;
    }

    public static boolean isSm2KeysAvailable() {
        return sm2PublicKey != null && sm2PrivateKey != null;
    }

    public static String getRsaKeyStatus() {
        return "RSA Public Key: " + (rsaPublicKey != null ? "Loaded" : "Not loaded") +
                "\nRSA Private Key: " + (rsaPrivateKey != null ? "Loaded" : "Not loaded");
    }

    public static String getSm2KeyStatus() {
        return "SM2 Public Key: " + (sm2PublicKey != null ? "Loaded" : "Not loaded") +
                "\nSM2 Private Key: " + (sm2PrivateKey != null ? "Loaded" : "Not loaded");
    }

    // ======== 专门的非对称加密方法 ========

    /**
     * 仅使用 RSA 加密（不经过多层加密）
     */
    public static String rsaEncryptOnly(String plainText) throws Exception {
        if (!isRsaKeysAvailable()) {
            throw new IllegalStateException("RSA keys not available");
        }
        byte[] data = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = rsaEncrypt(data);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 仅使用 RSA 解密
     */
    public static String rsaDecryptOnly(String encryptedText) throws Exception {
        if (!isRsaKeysAvailable()) {
            throw new IllegalStateException("RSA keys not available");
        }
        byte[] data = Base64.getDecoder().decode(encryptedText);
        byte[] decrypted = rsaDecrypt(data);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * 仅使用 SM2 加密
     */
    public static String sm2EncryptOnly(String plainText) throws Exception {
        if (!isSm2KeysAvailable()) {
            throw new IllegalStateException("SM2 keys not available");
        }
        byte[] data = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = sm2Encrypt(data);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 仅使用 SM2 解密
     */
    public static String sm2DecryptOnly(String encryptedText) throws Exception {
        if (!isSm2KeysAvailable()) {
            throw new IllegalStateException("SM2 keys not available");
        }
        byte[] data = Base64.getDecoder().decode(encryptedText);
        byte[] decrypted = sm2Decrypt(data);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    // ======== Base32 实现 ========
    private static class Base32 {
        private static final char[] ENCODE_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
        private static final int[] DECODE_TABLE = new int[128];

        static {
            Arrays.fill(DECODE_TABLE, -1);
            for (int i = 0; i < ENCODE_TABLE.length; i++) {
                DECODE_TABLE[ENCODE_TABLE[i]] = i;
                if (Character.isLetter(ENCODE_TABLE[i])) {
                    DECODE_TABLE[Character.toLowerCase(ENCODE_TABLE[i])] = i;
                }
            }
        }

        public static byte[] encode(byte[] data) {
            if (data.length == 0) return new byte[0];

            int outputLength = ((data.length + 4) / 5) * 8;
            byte[] output = new byte[outputLength];
            int bitCount = 0;
            int value = 0;
            int outputIndex = 0;

            for (byte b : data) {
                value = (value << 8) | (b & 0xFF);
                bitCount += 8;
                while (bitCount >= 5) {
                    bitCount -= 5;
                    output[outputIndex++] = (byte) ENCODE_TABLE[(value >>> bitCount) & 0x1F];
                }
            }

            if (bitCount > 0) {
                value <<= (5 - bitCount);
                output[outputIndex++] = (byte) ENCODE_TABLE[value & 0x1F];
            }

            while (outputIndex < output.length) {
                output[outputIndex++] = (byte) '=';
            }

            return output;
        }

        public static byte[] decode(byte[] data) {
            String str = new String(data, StandardCharsets.US_ASCII)
                    .replace("=", "")
                    .toUpperCase()
                    .trim();

            if (str.isEmpty()) return new byte[0];

            int bitCount = 0;
            int value = 0;
            int outputIndex = 0;
            byte[] output = new byte[(str.length() * 5 + 7) / 8];

            for (char c : str.toCharArray()) {
                int index = DECODE_TABLE[c];
                if (index == -1) continue;
                value = (value << 5) | index;
                bitCount += 5;
                if (bitCount >= 8) {
                    bitCount -= 8;
                    output[outputIndex++] = (byte) ((value >>> bitCount) & 0xFF);
                }
            }

            return Arrays.copyOf(output, outputIndex);
        }

        public static Encoder getEncoder() {
            return new Encoder();
        }

        public static Decoder getDecoder() {
            return new Decoder();
        }

        public static class Encoder {
            public byte[] encode(byte[] data) {
                return Base32.encode(data);
            }
        }

        public static class Decoder {
            public byte[] decode(byte[] data) {
                return Base32.decode(data);
            }
        }
    }

    // ======== 测试方法 ========

    public static boolean test() {
        try {
            String test = "{\"test\":\"Hello World\",\"number\":123}";
            System.out.println("Test data: " + test);

            String encrypted = encrypt(test);
            System.out.println("Encrypted: " + encrypted.substring(0, Math.min(100, encrypted.length())) + "...");

            String decrypted = decrypt(encrypted);
            System.out.println("Decrypted: " + decrypted);

            boolean success = test.equals(decrypted);
            System.out.println("Test " + (success ? "PASSED" : "FAILED"));

            if (!success) {
                System.out.println("Length mismatch: original=" + test.length() +
                        ", decrypted=" + decrypted.length());
            }

            return success;
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean testWithRsaSm2() {
        try {
            // 检查密钥是否可用
            if (!isRsaKeysAvailable() || !isSm2KeysAvailable()) {
                System.err.println("RSA or SM2 keys not available. Skipping RSA/SM2 test.");
                return false;
            }

            String test = "{\"test\":\"Hello World\",\"number\":123}";
            System.out.println("Test data: " + test);
            System.out.println("RSA Status: " + isRsaKeysAvailable());
            System.out.println("SM2 Status: " + isSm2KeysAvailable());

            // 测试使用 RSA 和 SM2
            String encrypted = encrypt(test, true, true);
            System.out.println("Encrypted (with RSA+SM2): " +
                    (encrypted.length() > 100 ? encrypted.substring(0, 100) + "..." : encrypted));

            String decrypted = decrypt(encrypted, true, true);
            System.out.println("Decrypted: " + decrypted);

            boolean success = test.equals(decrypted);
            System.out.println("RSA+SM2 Test " + (success ? "PASSED" : "FAILED"));

            // 测试只使用 RSA
            encrypted = encrypt(test, true, false);
            decrypted = decrypt(encrypted, true, false);
            System.out.println("RSA Only Test: " + (test.equals(decrypted) ? "PASSED" : "FAILED"));

            // 测试只使用 SM2
            encrypted = encrypt(test, false, true);
            decrypted = decrypt(encrypted, false, true);
            System.out.println("SM2 Only Test: " + (test.equals(decrypted) ? "PASSED" : "FAILED"));

            return success;
        } catch (Exception e) {
            System.err.println("RSA/SM2 test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ======== 主方法示例 ========

    public static void main(String[] args) {
        try {
            // 1. 测试原始的多层加密
            System.out.println("=== Testing Original Multi-layer Encryption ===");
            boolean originalTest = test();

            // 2. 检查 RSA/SM2 密钥状态
            System.out.println("\n=== Checking RSA/SM2 Key Status ===");
            System.out.println(getRsaKeyStatus());
            System.out.println(getSm2KeyStatus());

            // 3. 测试包含 RSA/SM2 的多层加密
            if (isRsaKeysAvailable() && isSm2KeysAvailable()) {
                System.out.println("\n=== Testing Multi-layer with RSA+SM2 ===");
                boolean rsaSm2Test = testWithRsaSm2();

                // 4. 测试单独的非对称加密
                System.out.println("\n=== Testing Standalone Asymmetric Encryption ===");
                String testData = "Secret message for asymmetric encryption";

                String rsaEncrypted = rsaEncryptOnly(testData);
                String rsaDecrypted = rsaDecryptOnly(rsaEncrypted);
                System.out.println("RSA Only Test: " +
                        (testData.equals(rsaDecrypted) ? "PASSED" : "FAILED"));

                String sm2Encrypted = sm2EncryptOnly(testData);
                String sm2Decrypted = sm2DecryptOnly(sm2Encrypted);
                System.out.println("SM2 Only Test: " +
                        (testData.equals(sm2Decrypted) ? "PASSED" : "FAILED"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}