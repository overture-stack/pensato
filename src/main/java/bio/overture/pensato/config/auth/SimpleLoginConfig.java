package bio.overture.pensato.config.auth;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Profile("simple")
@Configuration
@ConfigurationProperties(prefix = "auth.simple")
public class SimpleLoginConfig {

  @Getter @Setter
  private List<SimpleUser> logins;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SimpleUser {
    String username;
    String password;
  }

}
