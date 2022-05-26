package bio.overture.pensato.security;

import bio.overture.pensato.config.auth.EgoConfig;
import bio.overture.pensato.config.auth.EgoConfig.EgoProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.Optional;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.DefaultJwtParser;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Profile("ego")
@Component
public class EgoApiKeyAuthenticator implements PasswordAuthenticator {

  private final EgoProperties egoProperties;

  // Application JWT providing auth to introspect the API Keys on requests
  private static String egoToken;

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
   * Returns the stored ego token if it has not expired. If expired, it will fetch a new token from Ego.
   * @return
   */
  private Optional<String> getEgoToken() {
    return isStoredEgoTokenValid() ? Optional.of(egoToken) : fetchApplicationJwt();
  }

  private boolean isStoredEgoTokenValid() {
    if(egoToken == null) {
      log.debug("No stored Ego token.");
      return false;
    }

    try {
      // Attempt to parse stored ego token. If it is expired, an error will be thrown, otherwise it should be usable.
      DefaultJwtParser parser = new DefaultJwtParser();
      parser.parse(egoToken);
      return true;
    } catch (ExpiredJwtException expired) {
      log.debug("Stored access token has expired.");
      return false;
    } catch (MalformedJwtException malformed) {
      // Should never occur if ego working normally.
      log.warn("Stored EGO token is malfored!");
      log.warn(malformed.getMessage());
      return false;
    } catch (SignatureException invalidSignature) {
      // Should never occur if ego working normally.
      log.warn("Stored Ego token has invalid signature!");
      log.warn(invalidSignature.getMessage());
      return false;
    }
  }

  private Optional<String> fetchApplicationJwt() {
    try {
      val restTemplate = new RestTemplate();

      val requestUri = UriComponentsBuilder
              .fromHttpUrl(egoProperties.getEgoApiRootUrl())
              .path("/oauth/token")
              .queryParam("grant_type", "client_credentials")
              .queryParam("client_id", egoProperties.getClientId())
              .queryParam("client_secret", egoProperties.getClientSecret())
              .build().toUri();

      val response = restTemplate.postForEntity(requestUri, null, JsonNode.class);

      if ((response.getStatusCode() != HttpStatus.OK
              && response.getStatusCode() != HttpStatus.MULTI_STATUS)
              || !response.hasBody()) {
        return Optional.empty();
      }

      val accessToken = response.getBody().path("access_token").asText();
      log.info(accessToken);
      return Optional.of(accessToken);

    } catch (Exception e) {
      log.warn("Exception while fetching application JWT from Ego: {}", e.getMessage());
      log.debug("Stacktrace for exception:", e);
      return Optional.empty();
    }
  }

  /**
   * Checks token and email against Ego
   *
   * @param email Email provided by the user during authorization
   * @param token API Key that will be introspected by Ego
   * @return returns true if email and token are associated, and token contains valid scopes. False
   *     otherwise.
   */
  private boolean introspect(String email, String token) {
    try {
      val introspectUri = UriComponentsBuilder
        .fromHttpUrl(egoProperties.getEgoApiRootUrl())
        .path("/o/check_api_key")
        .queryParam("apiKey", token)
        .build().toUri();

      val template = new RestTemplate();
      val response =
        template.postForEntity(
          introspectUri,
          new HttpEntity<Void>(null, getJwtAuthHeader()),
          JsonNode.class);

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
        log.debug("Looking for scope: {}", scope);
        if (!scopes.contains(scope)) {
          log.debug("Missing scope: {}", scope);
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
   *
   * @param email User provided email
   * @param userId UserId obtained from an access token
   * @return True if valid, false otherwise
   */
  private boolean validateUser(String email, String userId) {
    try {

      val userDetailsUri = UriComponentsBuilder
              .fromHttpUrl(egoProperties.getEgoApiRootUrl())
              .path("/users/")
              .path(userId)
              .build().toUri();

      val template = new RestTemplate();
      val response =
              template.exchange(
                      userDetailsUri,
                      HttpMethod.GET,
                      new HttpEntity<Void>(null, getJwtAuthHeader()),
                      JsonNode.class);

      return email.equals(response.getBody().path("email").asText());
    } catch (Exception e) {
      log.error("Exception during ego user validation: {}", e.getMessage());
      log.debug("Stacktrace for exception", e);
      return false;
    }
  }

  private HttpHeaders getBasicAuthHeader() {
    val headers = new HttpHeaders();
    headers.setBasicAuth(egoProperties.getClientId(), egoProperties.getClientSecret());
    return headers;
  }

  private HttpHeaders getJwtAuthHeader() {
    val headers = new HttpHeaders();
    val token = getEgoToken();
    if (token.isPresent()) {
      headers.setBearerAuth(token.get());
    } else {
      log.warn("Unable to retrieve application JWT from Ego. Request validation will fail.");
    }
    return headers;
  }
}
