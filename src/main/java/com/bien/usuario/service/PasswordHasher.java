package com.bien.usuario.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

@ApplicationScoped
public class PasswordHasher {

    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_BYTES = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    public String hash(String password, String salt) {
        MessageDigest digest = digest();
        digest.update(HexFormat.of().parseHex(salt));
        byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashed);
    }

    public boolean matches(String password, String salt, String expectedHash) {
        String actualHash = hash(password, salt);
        return MessageDigest.isEqual(
                actualHash.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }

    private MessageDigest digest() {
        try {
            return MessageDigest.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(ALGORITHM + " no disponible", e);
        }
    }
}
