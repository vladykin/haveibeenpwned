package name.vladykin.haveibeenpwned;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public final class Password {

    private final String raw;
    private final String sha1;

    public Password(String rawPassword) {
        raw = Objects.requireNonNull(rawPassword, "rawPassword");
        sha1 = sha1(rawPassword);
    }

    public String raw() {
        return raw;
    }

    public String sha1() {
        return sha1;
    }

    private static String sha1(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            return toHex(md.digest(s.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA1 algorithm missing", e);
        }
    }

    private static String toHex(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return sha1;
    }
}
