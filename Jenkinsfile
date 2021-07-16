import groovy.json.JsonOutput

def version = "UNKNOWN"
def commit = "UNKNOWN"
def gitHubRegistry = "ghcr.io"
def gitHubRepo = "overture-stack/pensato"

def pom(path, target) {
    return [pattern: "${path}/pom.xml", target: "${target}.pom"]
}

def jar(path, target) {
    return [pattern: "${path}/target/*.jar",
            target         : "${target}.jar",
            excludePatterns: ["*-exec.jar"]
            ]
}

def tar(path, target) {
    return [pattern: "${path}/target/*.tar.gz",
            target : "${target}-dist.tar.gz"]
}

def runjar(path, target) {
    return [pattern: "${path}/target/*-exec.jar",
            target : "${target}-exec.jar"]
}

pipeline {
    agent {
        kubernetes {
            label 'pensato-executor'
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: jdk
    tty: true
    image: openjdk:11
    env:
      - name: DOCKER_HOST
        value: tcp://localhost:2375
    volumeMounts:
      - name: maven-cache
        mountPath: "/root/.m2"
  - name: dind-daemon
    image: docker:18.06-dind
    securityContext:
        privileged: true
    volumeMounts:
      - name: docker-graph-storage
        mountPath: /var/lib/docker
  - name: helm
    image: alpine/helm:2.12.3
    command:
    - cat
    tty: true
  - name: docker
    image: docker:18-git
    tty: true
    env:
      - name: DOCKER_HOST
        value: tcp://localhost:2375
  volumes:
  - name: docker-graph-storage
    emptyDir: {}
  - name: maven-cache
    emptyDir: {}
"""
        }
    }
    stages {
        stage('Prepare') {
            steps {
                script {
                    commit = sh(returnStdout: true, script: 'git describe --always').trim()
                }
                script {
                    version = readMavenPom().getVersion()
                }

            }
        }
        stage('Compile & Test') {
            steps {
                container('jdk') {
                    sh "./mvnw clean package"
                }
            }
        }

        stage('Build & Publish Develop') {
            when {
                branch "develop"
            }
            steps {
                container('docker') {
                    withCredentials([usernamePassword(credentialsId:'OvertureBioGithub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh "docker login ${gitHubRegistry} -u $USERNAME -p $PASSWORD"
                    }
                    sh "docker build --network=host -f Dockerfile . -t ${gitHubRegistry}/${gitHubRepo}:edge -t ${gitHubRegistry}/${gitHubRepo}:${commit}"
                    sh "docker push ${gitHubRegistry}/${gitHubRepo}:${commit}"
                    sh "docker push ${gitHubRegistry}/${gitHubRepo}:edge"
                }
            }
        }
        stage('Release & tag') {
            when {
                branch "master"
            }
            steps {
                container('docker') {
                    withCredentials([usernamePassword(credentialsId: 'OvertureBioGithub', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                        sh "git tag ${version}"
                        sh "git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/${gitHubRepo} --tags"
                    }
                }

                container('docker') {
                    withCredentials([usernamePassword(credentialsId:'OvertureBioGithub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh "docker login ${gitHubRegistry} -u $USERNAME -p $PASSWORD"
                    }
                    sh "docker build --network=host -f Dockerfile . -t ${gitHubRegistry}/${gitHubRepo}:latest -t ${gitHubRegistry}/${gitHubRepo}:${version}"
                    sh "docker push ${gitHubRegistry}/${gitHubRepo}:${version}"
                    sh "docker push ${gitHubRegistry}/${gitHubRepo}:latest"
                }
            }
        }

        stage('Destination SNAPSHOT') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'test-develop'
                }
            }
            steps {
                script {
                    repo = "dcc-snapshot/bio/overture"
                }
            }
        }

        stage('Destination release') {
            when {
                anyOf {
                    branch 'master'
                    branch 'test-master'
                }
            }
            steps {
                script {
                    repo = "dcc-release/bio/overture"
                }
            }
        }

        stage('Upload Artifacts') {
            when {
                anyOf {
                    branch 'master'
                    branch 'develop'
                }
            }
            steps {
                script {

                    project = "pensato"
                    versionName = "$version"

                    files = []
                    files.add([pattern: "pom.xml", target: "$repo/$project/$versionName/$project-${versionName}.pom"])

                    fileSet = JsonOutput.toJson([files: files])
                    pretty = JsonOutput.prettyPrint(fileSet)
                    print("Uploading files=${pretty}")
                }

                rtUpload(serverId: 'artifactory', spec: fileSet)
            }
        }
    }
}