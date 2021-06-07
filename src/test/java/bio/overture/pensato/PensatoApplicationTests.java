package bio.overture.pensato;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"default", "localdir"})
@SpringBootTest
class PensatoApplicationTests {

  @Test
  void contextLoads() {}
}
