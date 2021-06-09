package bio.overture.pensato.health;

import bio.overture.pensato.service.SftpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SftpHealthCheck implements HealthIndicator {

  private static final String MESSAGE_KEY = "sshd";

  private final SftpService sftpService;

  @Autowired
  public SftpHealthCheck(SftpService sftpService) {
    this.sftpService = sftpService;
  }

  @Override
  public Health health() {
    if (sftpService.isRunning()) {
      return Health.up().withDetail(MESSAGE_KEY, "SSHD is running.").build();
    } else {
      return Health.down().withDetail(MESSAGE_KEY, "SSHD is down.").build();
    }
  }
}
