package bio.overture.pensato.service;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import javax.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.Handle;
import org.apache.sshd.server.subsystem.sftp.SftpEventListener;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SftpService {

  private final PasswordAuthenticator passwordAuthenticator;
  private final FileSystemFactory fileSystemFactory;
  private final int port;

  @Autowired
  public SftpService(
      PasswordAuthenticator passwordAuthenticator,
      FileSystemFactory fileSystemFactory,
      @Value("${sftp.port}") int port) {
    this.passwordAuthenticator = passwordAuthenticator;
    this.fileSystemFactory = fileSystemFactory;
    this.port = port;
  }

  @PostConstruct
  public void startServer() {
    start();
  }

  @SneakyThrows
  private void start() {
    SshServer sshd = SshServer.setUpDefaultServer();

    val sftpFactory = new SftpSubsystemFactory();
    sftpFactory.addSftpEventListener(new SftpSetupListener());

    sshd.setPort(port);
    sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("host.ser")));
    sshd.setSubsystemFactories(Collections.singletonList(sftpFactory));
    sshd.setPasswordAuthenticator(passwordAuthenticator);
    sshd.setFileSystemFactory(fileSystemFactory);
    sshd.start();
    log.info("SFTP server started on port {}", sshd.getPort());
  }

  private class SftpSetupListener implements SftpEventListener {

    @Override
    public void initialized(ServerSession session, int version) {
      log.info(session.toString());
    }

    @Override
    public void open(ServerSession session, String remoteHandle, Handle localHandle)
        throws IOException {
      log.info(session.toString());
    }
  }
}
