# pensato
SFTP based file sharing.

## Auth Configuration

### Default
The default auth is a No Login authenticator that rejects all users.
This is a safe default and requires no configuration.

### Simple
```
-Dspring.profiles.active=simple
```
```yaml
auth:
  simple:
    logins:
        # SHA256 passwords
      - username: foo
        password: fcde2b2edba56bf408601fb721fe9b5c338d10ee429ea04fae5511b68fbf8fb9
      - username: biz
        password: baa5a0964d3320fbc0c6a922140453c8513ea24ab8fd0577034804a967248096
```

This is a simple auth scheme that uses users configured in the application configuration.

Passwords are SHA256 hashes. When generating your own ensure you strip newlines. For example:
```bash
echo -n mynewpass | sha256sum
```

### Ego
```
-Dspring.profiles.active=simple
```
Pensato can be configured as a client of Ego. Users login with their emails and API Keys.

```yaml
auth:
  ego:
    clientId: pensato
    clientSecret: pensatosecret
    introspectionUri: <.../o/check_api_key>
    userInfoUri: <.../users>
    scopes:
      - POLICYNAME.READ
```