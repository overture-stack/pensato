package bio.overture.pensato.service;

import java.io.File;
import java.util.Collections;
import javax.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SftpService {

  private final PasswordAuthenticator passwordAuthenticator;

  @Autowired
  public SftpService(PasswordAuthenticator passwordAuthenticator) {
    this.passwordAuthenticator = passwordAuthenticator;
  }

  @PostConstruct
  public void startServer() {
    start();
  }

  @SneakyThrows
  private void start() {
    SshServer sshd = SshServer.setUpDefaultServer();
    sshd.setPort(2222);
    sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("host.ser")));
    sshd.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
    sshd.setPasswordAuthenticator(passwordAuthenticator);
    sshd.start();
    log.info("SFTP server started");
  }
}
