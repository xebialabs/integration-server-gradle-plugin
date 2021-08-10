#!groovy
@Library('jenkins-pipeline-libs@master')
import com.xebialabs.pipeline.globals.Globals

pipeline {

    agent none

    parameters {
        string(name: 'jdkVersion',
                defaultValue: 'OpenJDK 11.0.12',
                description: 'Configuration to run server on an environment with designated jdk version')
        string(name: 'slaveNode', defaultValue: 'xlr||xld', description: 'Node label where steps would be executed.')
    }

    environment {
        REPOSITORY_NAME = 'integration-server-gradle-plugin'
        GRADLE_OPTS = "-XX:MaxPermSize=256m -Xmx1024m  -Djsse.enableSNIExtension=true"
    }

    stages {
        stage('Run test') {
            agent { label params.slaveNode }
            steps {
                withEnv(Globals.java11Env(this, params.jdkVersion)) {
                    sh "./gradlew clean test"
                }
            }
        }
    }
    post {
        always {
            node('xld') {
                step([$class: 'ClaimPublisher'])
            }
        }
    }
}
