package bio.overture.pensato.health;

import bio.overture.pensato.service.SshdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SftpHealthCheck implements HealthIndicator {

  private static final String MESSAGE_KEY = "sshd";

  private final SshdService sshdService;

  @Autowired
  public SftpHealthCheck(SshdService sshdService) {
    this.sshdService = sshdService;
  }

  @Override
  public Health health() {
    if (sshdService.isRunning()) {
      return Health.up().withDetail(MESSAGE_KEY, "SSHD is running.").build();
    } else {
      return Health.down().withDetail(MESSAGE_KEY, "SSHD is down.").build();
    }
  }
}
