package bio.overture.pensato.security;

import bio.overture.pensato.config.auth.SimpleLoginConfig;
import bio.overture.pensato.config.auth.SimpleLoginConfig.SimpleUser;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("simple")
@Component
public class SimpleAuthenticator implements PasswordAuthenticator {

  private final Map<String, String> logins;

  @Autowired
  public SimpleAuthenticator(SimpleLoginConfig config) {
    this.logins =
        config.getLogins().stream()
            .collect(
                Collectors.toUnmodifiableMap(SimpleUser::getUsername, SimpleUser::getPassword));
  }

  @SneakyThrows
  @Override
  public boolean authenticate(String username, String password, ServerSession serverSession)
      throws PasswordChangeRequiredException {
    val digest = MessageDigest.getInstance("SHA-256");

    val auth =
        Optional.ofNullable(this.logins.get(username))
            .map(
                p -> {
                  val hash = bytesToHex(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
                  return p.equals(hash);
                })
            .orElse(false);

    if (!auth) {
      log.info("Authentication failed for provided user: {}", username);
    }

    return auth;
  }

  private static String bytesToHex(byte[] hash) {
    val hexString = new StringBuilder(2 * hash.length);
    for (val b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }
}
