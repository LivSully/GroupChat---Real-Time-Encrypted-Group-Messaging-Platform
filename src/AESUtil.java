package src;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtil {
    private static final String ALGORITHM = "AES";
    private static final String KEY = "1234567890123456"; // 16-byte secret key (128-bit)

    // Method that takes in a string and returns the encrypted version of that string using AES encryption
    public static String encrypt(String data) throws Exception {
        SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM); 
        cipher.init(Cipher.ENCRYPT_MODE, key); 
        byte[] encrypted = cipher.doFinal(data.getBytes()); 
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // Method that takes in an encrypted array of bytes and returns the decrypted version of that array using AES decryption
    public static byte[] decryptImage(byte[] encryptedBytes) throws Exception {
        SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedBytes);
    }

    // Method that takes in an array of bytes representing an image and returns the encrypted version of that array using AES encryption
    public static byte[] encryptImage(byte[] imageBytes) throws Exception {
        SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(imageBytes);
    }

    // Method that takes in an encrypted string and returns the decrypted version of that string using AES decryption
    public static String decrypt(String encryptedData) throws Exception { 
        SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decrypted);
    }
}
