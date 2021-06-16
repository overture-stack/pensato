package bio.overture.pensato.service;

import bio.overture.pensato.listener.LoggingScpListener;
import bio.overture.pensato.listener.LoggingSftpListener;
import java.io.File;
import java.util.*;
import javax.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SshdService {

  private final PasswordAuthenticator passwordAuthenticator;
  private final FileSystemFactory fileSystemFactory;
  private final Environment environment;
  private final int port;

  private SshServer sshd;

  @Autowired
  public SshdService(
      PasswordAuthenticator passwordAuthenticator,
      FileSystemFactory fileSystemFactory,
      Environment environment,
      @Value("${sftp.port}") int port) {
    this.passwordAuthenticator = passwordAuthenticator;
    this.fileSystemFactory = fileSystemFactory;
    this.environment = environment;
    this.port = port;
  }

  @PostConstruct
  public void startServer() {
    start();
  }

  public boolean isRunning() {
    return sshd.isOpen();
  }

  @SneakyThrows
  private void start() {
    sshd = SshServer.setUpDefaultServer();

    val sftpFactory = new SftpSubsystemFactory();
    sftpFactory.addSftpEventListener(new LoggingSftpListener());

    sshd.setPort(port);
    sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("host.ser")));
    sshd.setSubsystemFactories(Collections.singletonList(sftpFactory));
    sshd.setPasswordAuthenticator(passwordAuthenticator);
    sshd.setFileSystemFactory(fileSystemFactory);

    /*
     * SCP commands show up differently in logs.
     * Should only nable only if DEBUG logging is enabled
     * in the org.apache.sshd.common.scp package.
     */
    if (Set.of(environment.getActiveProfiles()).contains("scp")) {
      val scpFactory = new ScpCommandFactory();
      scpFactory.addEventListener(new LoggingScpListener());
      sshd.setCommandFactory(scpFactory);
    }

    sshd.start();
    log.info("SFTP server started on port {}", sshd.getPort());
  }
}
