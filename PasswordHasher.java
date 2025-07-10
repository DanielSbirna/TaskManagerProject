package com.example.taskmanager.data;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import android.util.Base64; //encoding / decoding byte arrays to Strings

public class PasswordHasher {
    //higher is secure but slow
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16; //16 bytes = 128 bits

    // Generates a random salt for each password
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    // Hashes a password using PBKDF2 with a given salt
    public static String hashPassword(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); // Modern, strong algorithm
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.encodeToString(hash, Base64.DEFAULT); // Encode hash to String for storage
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null; // Handle error appropriately in calling code
        }
    }

    // Verifies a password against a stored hash and salt
    public static boolean verifyPassword(String password, String storedHash, byte[] storedSalt) {
        String newHash = hashPassword(password, storedSalt);
        return newHash != null && newHash.equals(storedHash);
    }

    // Helper to convert byte array to Base64 String for storing salt
    public static String saltToString(byte[] salt) {
        return Base64.encodeToString(salt, Base64.DEFAULT);
    }

    // Helper to convert Base64 String back to byte array for retrieving salt
    public static byte[] stringToSalt(String saltString) {
        return Base64.decode(saltString, Base64.DEFAULT);
    }
}

