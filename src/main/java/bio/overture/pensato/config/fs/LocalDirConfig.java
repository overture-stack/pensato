package bio.overture.pensato.config.fs;

import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("localdir")
@Configuration
@ConfigurationProperties(prefix = "fs")
public class LocalDirConfig {

  @Getter @Setter private String localDir;

  @Bean
  public FileSystemFactory getFileSystemFactory() {
    return new VirtualFileSystemFactory(Path.of(localDir));
  }
}
