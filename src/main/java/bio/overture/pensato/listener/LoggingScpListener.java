package bio.overture.pensato.listener;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.scp.ScpTransferEventListener;

@Slf4j
public class LoggingScpListener implements ScpTransferEventListener {

  @Override
  public void startFileEvent(
      ScpTransferEventListener.FileOperation op,
      Path file,
      long length,
      Set<PosixFilePermission> perms)
      throws IOException {
    log.info(op.toString(), file.toString(), perms.toString());
  }
}
