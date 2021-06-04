package bio.overture.pensato.security;

import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("ego")
@Component
public class EgoApiKeyAuthenticator implements PasswordAuthenticator {

  @Override
  public boolean authenticate(String username, String password, ServerSession serverSession) throws PasswordChangeRequiredException {
    return username.equals("test") && password.equals("password");
  }
}
