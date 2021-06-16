package bio.overture.pensato.security;

import bio.overture.pensato.config.auth.EgoConfig;
import bio.overture.pensato.config.auth.EgoConfig.EgoProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Profile("ego")
@Component
public class EgoApiKeyAuthenticator implements PasswordAuthenticator {

  private final EgoProperties egoProperties;

  @Autowired
  public EgoApiKeyAuthenticator(EgoConfig config) {
    this.egoProperties = config.getEgo();
  }

  @Override
  public boolean authenticate(String username, String password, ServerSession serverSession)
      throws PasswordChangeRequiredException {
    log.info("authenticating!");
    val result = introspect(username, password);
    if (result) {
      log.info("Successfully authenticated {}", username);
    } else {
      log.info("Authentication failed for provided user: {}", username);
    }

    return result;
  }

  /**
   * Checks token and email against against Ego
   * @param email Email provided by the user during authorization
   * @param token API Key that will be introspected by Ego
   * @return returns true if email and token are associated, and token contains valid scopes. False otherwise.
   */
  private boolean introspect(String email, String token) {
    try {
      val template = new RestTemplate();
      val response =
          template.postForEntity(
              egoProperties.getIntrospectionUri() + "?apiKey=" + token, setupHttpEntity(egoProperties), JsonNode.class);

      // Response is okay
      if ((response.getStatusCode() != HttpStatus.OK
              && response.getStatusCode() != HttpStatus.MULTI_STATUS)
          || !response.hasBody()) {
        return false;
      }

      // User and Token are associated to each other in ego
      var isValidUserForToken = validateUser(email, response.getBody().path("user_id").asText());
      if (!isValidUserForToken) {
        return false;
      }

      // Token is correctly structured
      val scopeJsonNode = response.getBody().path("scope");
      if (scopeJsonNode.isMissingNode()) {
        return false;
      }

      // Token contains all required scopes
      val scopesArrayNode = (ArrayNode) scopeJsonNode;
      val scopes = new ArrayList<String>();
      scopesArrayNode.forEach(jsonNode -> scopes.add(jsonNode.asText()));
      for (val scope : egoProperties.getScopes()) {
        if (!scopes.contains(scope)) {
          return false;
        }
      }

      return true;
    } catch (Exception e) {
      log.warn("Exception during apiKey introspection: {}", e.getMessage());
      log.debug("Stacktrace for exception:", e);
      return false;
    }
  }

  /**
   * Validates that the provided email belongs to the user
   * @param email User provided email
   * @param userId UserId obtained from an access token
   * @return True if valid, false otherwise
   */
  private boolean validateUser(String email, String userId) {
    try {
      val template = new RestTemplate();
      val response =
          template.exchange(
              egoProperties.getUserInfoUri() + "/" + userId,
              HttpMethod.GET,
              setupHttpEntity(egoProperties),
              JsonNode.class);

      return email.equals(response.getBody().path("email").asText());
    } catch (Exception e) {
      log.error("Exception during ego user validation: {}", e.getMessage());
      log.debug("Stacktrace for exception", e);
      return false;
    }
  }

  private static HttpEntity<Void> setupHttpEntity(EgoProperties properties) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBasicAuth(properties.getClientId(), properties.getClientSecret());
    return new HttpEntity<Void>(null, headers);
  }
}
