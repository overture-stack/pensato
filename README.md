<h1 align="center">Pensato</h1>
<p align="center">General Purpose SFTP/SCP Server</p>
<p align="center">
    <a href="https://github.com/overture-stack/pensato">
        <img alt="Beta" 
            title="Beta" 
            src="http://www.overture.bio/img/progress-horizontal-beta.svg" width="320" />
    </a>
</p>

## Introduction
Pensato is a microservice written in Java 11 + Spring Boot for providing SFTP and eventually SCP server
functionality. It leverages Apache MINA as the underlying SSH library and the Java NIO FileSystem
APIs for providing the backing storage.

## Build
Currently, for S3 functionality, Pensato requires a fork of s3fs which can be found here: https://github.com/andricDu/Amazon-S3-FileSystem-NIO2
```yaml
mvn clean package
```

## Configure
### Auth Configuration

#### Default
The default auth is a No Login authenticator that rejects all users.
This is a safe default and requires no configuration.

#### Simple
This is a simple auth scheme that uses users configured in the application configuration.
Passwords are SHA256 hashes. When generating your own ensure you strip newlines. See example below.

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

```bash
echo -n mynewpass | sha256sum
```

#### Ego
Pensato can be configured as a client of Ego. Users login with their emails and API Keys.
Every scope in the `scopes` list must be present in the API key. 


```
-Dspring.profiles.active=ego
```

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

#### Keycloak
:construction: Under Construction :construction:

### Storage Configuration

#### Local Directory
The simplest way to provide SFTP is by serving up a local directory on 
the filesystem where Pensato is running. 
```
-Dspring.profiles.active=localdir
```
```yaml
fs:
  localDir: /tmp
```

#### S3
It can be desirable to use object storage as a backend. Pensato supports using an S3 bucket either hosted
on amazon, or provided by something like Ceph or Minio. 
```
-Dspring.profiles.active=s3
```
```yaml
fs:
  s3:
    endpoint: https://localhost:9000
    accessKey: admin
    secretKey: password
    bucket: pensato
    dir: sftp # This is a directory within the bucket.
```

#### Azure Blob Storage
:construction: Under Construction :construction:

#### HDFS
:construction: Under Construction :construction:

