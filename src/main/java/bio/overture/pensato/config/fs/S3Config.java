package bio.overture.pensato.config.fs;

import com.google.common.collect.ImmutableMap;
import com.upplication.s3fs.AmazonS3Factory;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Profile("s3")
@Configuration
@ConfigurationProperties(prefix = "fs.s3")
public class S3Config {

  @Getter @Setter private String endpoint;
  @Getter @Setter private String accessKey;
  @Getter @Setter private String secretKey;
  @Getter @Setter private String bucket;
  @Getter @Setter private String dir;

  @SneakyThrows
  @Bean
  public FileSystemFactory getFileSystemFactory() {
    Map<String, ?> env =
        ImmutableMap.<String, Object>builder()
            .put(AmazonS3Factory.ACCESS_KEY, accessKey)
            .put(AmazonS3Factory.SECRET_KEY, secretKey)
            .put(AmazonS3Factory.PATH_STYLE_ACCESS, true)
            .put(AmazonS3Factory.SIGNER_OVERRIDE, "AWSS3V4SignerType")
            .put(AmazonS3Factory.ENDPOINT_URL, endpoint)
            .build();

    val fs =
        FileSystems.newFileSystem(
            URI.create("s3:///"), env, Thread.currentThread().getContextClassLoader());

    log.info(bucket);
    return new VirtualFileSystemFactory(fs.getPath("/" + bucket + "/" + dir + "/"));
  }
}
