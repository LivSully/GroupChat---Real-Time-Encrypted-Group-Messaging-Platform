package src;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/*
 * Utility class to handle AES encryption and decryption.
 */
public class AESUtil {
    private static final String ALGORITHM = "AES";
    private static final String KEY = "1234567890123456"; // 16-byte secret key (128-bit)

    /**
     * Encrypts a plain text string using AES.
     * 
     * @param data The plain text to encrypt.
     * @return The encrypted Base64-encoded string.
     */
    public static String encrypt(String data) throws Exception {
        SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGORITHM); // Create a secret key specification based on
                                                                          // the provided key
        Cipher cipher = Cipher.getInstance(ALGORITHM); // Get a cipher instance for AES
        cipher.init(Cipher.ENCRYPT_MODE, key); // Initialize the cipher in encryption mode with the secret key
        byte[] encrypted = cipher.doFinal(data.getBytes()); // Perform the encryption operation
        return Base64.getEncoder().encodeToString(encrypted); // Encode to Base64 for safe transmission
    }

    /**
     * Decrypts an AES-encrypted Base64 string.
     * 
     * @param encryptedData The Base64-encoded encrypted text.
     * @return The decrypted plain text.
     */
    public static String decrypt(String encryptedData) throws Exception { // the encryption method but in reverse
        SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decrypted);
    }
}
