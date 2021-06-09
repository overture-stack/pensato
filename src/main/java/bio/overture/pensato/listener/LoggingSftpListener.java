package bio.overture.pensato.listener;

import static java.lang.String.format;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.DirectoryHandle;
import org.apache.sshd.server.subsystem.sftp.FileHandle;
import org.apache.sshd.server.subsystem.sftp.Handle;
import org.apache.sshd.server.subsystem.sftp.SftpEventListener;

@Slf4j
public class LoggingSftpListener implements SftpEventListener {

  private static final String MESSAGE_TEMPLATE = "user: %s, action: %s, message: %s";

  @Override
  public void initialized(ServerSession session, int version) {
    val message = format("Version %d", version);
    log.info(format(MESSAGE_TEMPLATE, session.getUsername(), "INITIALIZED", message));
  }

  public void destroying(ServerSession session) {
    val message = "destroying...";
    log.debug(format(MESSAGE_TEMPLATE, session.getUsername(), "DESTROYING", message));
  }

  public void opening(ServerSession session, String remoteHandle, Handle localHandle)
      throws IOException {
    val message = format("remote: %s, local: %s", remoteHandle, localHandle);
    log.debug(format(MESSAGE_TEMPLATE, session.getUsername(), "OPENING", message));
  }

  public void open(ServerSession session, String remoteHandle, Handle localHandle)
      throws IOException {
    val message = format("remote: %s, local: %s", remoteHandle, localHandle);
    log.info(format(MESSAGE_TEMPLATE, session.getUsername(), "OPEN", message));
  }

  /** This logs out the directory listing. Triggered when `ls`ing a directory. */
  public void read(
      ServerSession session,
      String remoteHandle,
      DirectoryHandle localHandle,
      Map<String, Path> entries)
      throws IOException {
    val message =
        format("remote: %s, local: %s, entries: %s", remoteHandle, localHandle, entries.toString());
    log.debug(format(MESSAGE_TEMPLATE, session.getUsername(), "READ", message));
  }

  public void reading(
      ServerSession session,
      String remoteHandle,
      FileHandle localHandle,
      long offset,
      byte[] data,
      int dataOffset,
      int dataLen)
      throws IOException {
    val message = format("remote: %s, local: %s, offset: %d", remoteHandle, localHandle, offset);
    log.debug(format(MESSAGE_TEMPLATE, session.getUsername(), "READING", message));
  }

  public void read(
      ServerSession session,
      String remoteHandle,
      FileHandle localHandle,
      long offset,
      byte[] data,
      int dataOffset,
      int dataLen,
      int readLen,
      Throwable thrown)
      throws IOException {
    val message = format("remote: %s, local: %s, offset: %d", remoteHandle, localHandle, offset);
    if (offset == 0) {
      log.info(format(MESSAGE_TEMPLATE, session.getUsername(), "READ", message));
    } else {
      log.debug(format(MESSAGE_TEMPLATE, session.getUsername(), "READ", message));
    }
  }

  public void writing(
      ServerSession session,
      String remoteHandle,
      FileHandle localHandle,
      long offset,
      byte[] data,
      int dataOffset,
      int dataLen)
      throws IOException {
    val message = format("remote: %s, local: %s, offset: %d", remoteHandle, localHandle, offset);
    log.debug(format(MESSAGE_TEMPLATE, session.getUsername(), "WRITING", message));
  }

  public void written(
      ServerSession session,
      String remoteHandle,
      FileHandle localHandle,
      long offset,
      byte[] data,
      int dataOffset,
      int dataLen,
      Throwable thrown)
      throws IOException {
    val message = format("remote: %s, local: %s, offset: %d", remoteHandle, localHandle, offset);
    if (offset == 0) {
      log.info(format(MESSAGE_TEMPLATE, session.getUsername(), "WRITTEN", message));
    } else {
      log.debug(format(MESSAGE_TEMPLATE, session.getUsername(), "WRITTEN", message));
    }
  }

  public void blocking(
      ServerSession session,
      String remoteHandle,
      FileHandle localHandle,
      long offset,
      long length,
      int mask)
      throws IOException {
    val message = format("remote: %s, local: %s, offset: %d", remoteHandle, localHandle, offset);
    log.debug(format(MESSAGE_TEMPLATE, session.getUsername(), "BLOCKING", message));
  }

  public void blocked(
      ServerSession session,
      String remoteHandle,
      FileHandle localHandle,
      long offset,
      long length,
      int mask,
      Throwable thrown)
      throws IOException {
    val message = format("remote: %s, local: %s, offset: %d", remoteHandle, localHandle, offset);
    log.info(format(MESSAGE_TEMPLATE, session.getUsername(), "BLOCKED", message));
  }

  public void unblocking(
      ServerSession session, String remoteHandle, FileHandle localHandle, long offset, long length)
      throws IOException {
    val message = format("remote: %s, local: %s, offset: %d", remoteHandle, localHandle, offset);
    log.debug(format(MESSAGE_TEMPLATE, session.getUsername(), "UNBLOCKING", message));
  }

  public void unblocked(
      ServerSession session,
      String remoteHandle,
      FileHandle localHandle,
      long offset,
      long length,
      Throwable thrown)
      throws IOException {
    val message = format("remote: %s, local: %s, offset: %d", remoteHandle, localHandle, offset);
    log.info(format(MESSAGE_TEMPLATE, session.getUsername(), "UNBLOCKED", message));
  }

  public void close(ServerSession session, String remoteHandle, Handle localHandle) {
    val message = format("remote: %s, local: %s", remoteHandle, localHandle);
    log.info(format(MESSAGE_TEMPLATE, session.getUsername(), "CLOSE", message));
  }

  public void creating(ServerSession session, Path path, Map<String, ?> attrs) throws IOException {
    val message = format("path: %s, attrs: %s", path.toString(), attrs.toString());
    log.info(format(MESSAGE_TEMPLATE, session.getUsername(), "CREATING", message));
  }

  public void created(ServerSession session, Path path, Map<String, ?> attrs, Throwable thrown)
      throws IOException {
    val message = format("path: %s, attrs: %s", path.toString(), attrs.toString());
    log.info(format(MESSAGE_TEMPLATE, session.getUsername(), "CREATED", message));
  }

  public void moving(ServerSession session, Path srcPath, Path dstPath, Collection<CopyOption> opts)
      throws IOException {
    val message = format("src: %s, dest: %s", srcPath.toString(), dstPath.toString());
    log.debug(format(MESSAGE_TEMPLATE, session.getUsername(), "MOVING", message));
  }

  public void moved(
      ServerSession session,
      Path srcPath,
      Path dstPath,
      Collection<CopyOption> opts,
      Throwable thrown)
      throws IOException {
    val message = format("src: %s, dest: %s", srcPath.toString(), dstPath.toString());
    log.info(format(MESSAGE_TEMPLATE, session.getUsername(), "MOVED", message));
  }

  public void removing(ServerSession session, Path path) throws IOException {
    val message = format("path: %s", path.toString());
    log.debug(format(MESSAGE_TEMPLATE, session.getUsername(), "REMOVING", message));
  }

  public void removed(ServerSession session, Path path, Throwable thrown) throws IOException {
    val message = format("path: %s", path.toString());
    log.info(format(MESSAGE_TEMPLATE, session.getUsername(), "REMOVED", message));
  }

  public void linking(ServerSession session, Path source, Path target, boolean symLink)
      throws IOException {
    val message = format("source: %s, target: %s", source.toString(), target.toString());
    log.debug(format(MESSAGE_TEMPLATE, session.getUsername(), "LINKING", message));
  }

  public void linked(
      ServerSession session, Path source, Path target, boolean symLink, Throwable thrown)
      throws IOException {
    val message = format("source: %s, target: %s", source.toString(), target.toString());
    log.info(format(MESSAGE_TEMPLATE, session.getUsername(), "LINKED", message));
  }

  public void modifyingAttributes(ServerSession session, Path path, Map<String, ?> attrs)
      throws IOException {
    val message = format("path: %s, attrs: %s", path.toString(), attrs.toString());
    log.debug(format(MESSAGE_TEMPLATE, session.getUsername(), "MODIFYING_ATTRIBUTES", message));
  }

  public void modifiedAttributes(
      ServerSession session, Path path, Map<String, ?> attrs, Throwable thrown) throws IOException {
    val message = format("path: %s, attrs: %s", path.toString(), attrs.toString());
    log.info(format(MESSAGE_TEMPLATE, session.getUsername(), "MODIFIED_ATTRIBUTES", message));
  }
}
