package bio.overture.pensato.security;

import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("default")
@Component
public class DefaultNoLoginAuthenticator implements PasswordAuthenticator {

  @Override
  public boolean authenticate(String s, String s1, ServerSession serverSession)
      throws PasswordChangeRequiredException {
    return false;
  }
}
