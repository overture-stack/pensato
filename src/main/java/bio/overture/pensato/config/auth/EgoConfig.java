package bio.overture.pensato.config.auth;

import java.util.List;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("ego")
@Configuration
@ConfigurationProperties(prefix = "auth")
public class EgoConfig {

  @Getter @Setter private EgoProperties ego;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class EgoProperties {
    String clientId;
    String clientSecret;
    String egoApiRootUrl;
    List<String> scopes;
  }
}
