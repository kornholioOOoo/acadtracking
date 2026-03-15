package Config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Password hashing for database storage. Uses SHA-256 with a per-entry salt.
 * Stored format: "H:" + salt_hex + ":" + hash_hex (so we can verify and migrate plain text).
 */
public class PasswordHash {

    private static final String PREFIX = "H:";
    private static final int SALT_BYTES = 16;
    private static final String ALG = "SHA-256";

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private static byte[] fromHex(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /** Returns a hashed string to store in the database (prefix H:). */
    public static String hash(String plainPassword) {
        if (plainPassword == null) plainPassword = "";
        try {
            byte[] salt = new byte[SALT_BYTES];
            new SecureRandom().nextBytes(salt);
            byte[] hash = digest(salt, plainPassword.getBytes(StandardCharsets.UTF_8));
            return PREFIX + toHex(salt) + ":" + toHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    /** Verifies the given plain password against the stored value (hashed or legacy plain). */
    public static boolean verify(String plainPassword, String stored) {
        if (stored == null) stored = "";
        if (plainPassword == null) plainPassword = "";
        if (stored.startsWith(PREFIX)) {
            int firstColon = PREFIX.length();
            int secondColon = stored.indexOf(':', firstColon);
            if (secondColon == -1) return false;
            String saltHex = stored.substring(firstColon, secondColon);
            String hashHex = stored.substring(secondColon + 1);
            try {
                byte[] salt = fromHex(saltHex);
                byte[] expectedHash = fromHex(hashHex);
                byte[] actualHash = digest(salt, plainPassword.getBytes(StandardCharsets.UTF_8));
                return MessageDigest.isEqual(expectedHash, actualHash);
            } catch (Exception e) {
                return false;
            }
        }
        return plainPassword.equals(stored);
    }

    /** If stored is plain text, re-hash and return new value to update DB; otherwise return null. */
    public static String migrateToHashIfPlain(String plainPassword, String stored) {
        if (stored == null || stored.startsWith(PREFIX)) return null;
        if (!plainPassword.equals(stored)) return null;
        return hash(plainPassword);
    }

    private static byte[] digest(byte[] salt, byte[] password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALG);
        md.update(salt);
        md.update(password);
        return md.digest();
    }
}
