package com.gruelbox.orko.auth;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.google.common.base.Charsets;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class Hasher {

  private static final Pattern EXTRACT_HASH = Pattern.compile("HASH\\(.*\\)");

  public static void main(String[] args) throws IOException {
    Injector injector = Guice.createInjector();
    if (args[0].equals("--salt")) {
      System.out.println("Salt: " + injector.getInstance(Hasher.class).salt());
    } else if (args[0].equals("--hash")) {
      System.out.println("Password: " + args[1]);
      System.out.println("Salt: " + args[2]);
      System.out.println("Hashed: " + injector.getInstance(Hasher.class).hash(args[1], args[2]));
    }
  }

  public boolean isHash(String storedPassword) {
    return EXTRACT_HASH.matcher(storedPassword).find();
  }

  public String salt() {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    return Base64.getEncoder().encodeToString(salt);
  }

  public String hashWithString(String value, String stringSalt) {
    String salt = Base64.getEncoder().encodeToString(stringSalt.getBytes(Charsets.UTF_8));
    return hash(value, salt);
  }

  public String hash(String value, String salt) {
    KeySpec spec = new PBEKeySpec(value.toCharArray(), Base64.getDecoder().decode(salt), 65536, 256);
    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      byte[] hash = factory.generateSecret(spec).getEncoded();
      return "HASH(" + Base64.getEncoder().encodeToString(hash) + ")";
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException(e);
    }
  }
}
