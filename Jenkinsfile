pipeline {

  agent { label 'jenkins-aql-node-3' }

  environment {
     IMAGE_NAME = 'lcastaa/git-hub-scm'
     IMAGE_TAG = 'latest'
     DOCKER_CREDENTIALS_ID = 'docker-login'
  }

  stages {

    stage('Build') {
      notifyDiscord("Build started on branch ${env.BRANCH_NAME}")
      when {
        expression {
          return isBuildOrPR(env.BRANCH_NAME)
        }
      }
      steps {
        echo "Building branch: ${env.BRANCH_NAME}"
        sh './mvnw clean compile'
      }
    }

    stage('Test') {
      when {
        expression {
          return isBuildOrPR(env.BRANCH_NAME)
        }
      }
      steps {
        echo "Testing branch: ${env.BRANCH_NAME}"
        sh './mvnw test'
      }
    }

    stage('Post-Test Tasks') {
      when {
        expression {
          return isBuildOrPR(env.BRANCH_NAME)
        }
      }
      steps {
        echo "Publishing test results"
      }
    }

    stage('Packing executable JAR') {
      when {
        expression {
          return isBuildOrPR(env.BRANCH_NAME)
        }
      }
      steps {
       sh './mvnw clean package -DskipTests'
      }
    }

    stage('Building Docker Images') {
      when {
        expression {
          return isFullDeployBranch(env.BRANCH_NAME)
        }
      }
      steps {
        sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
      }
    }

    stage('Publishing Docker Images') {
      when {
        expression {
          return isFullDeployBranch(env.BRANCH_NAME)
        }
      }
      steps {
        withCredentials([usernamePassword(credentialsId: 'docker-login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh '''
             echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
             docker push ${IMAGE_NAME}:${IMAGE_TAG}
             docker logout
             '''
        }
      }
    }

    stage('Deploying docker images') {
      when {
        expression {
          return isFullDeployBranch(env.BRANCH_NAME)
        }
      }
      steps {
        script {
          sh "chmod +x ./pipeline-scripts/clean-containers.sh"
          def cleanupStatus = sh(script: "./pipeline-scripts/clean-containers.sh janitor-tool", returnStatus: true)
          if (cleanupStatus != 0) {
            error("Cleanup script failed with exit code ${cleanupStatus}. Stopping deployment.")
          }
          withCredentials([usernamePassword(credentialsId: 'docker-login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
            sh """
              echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
              docker compose pull
            """
          }
          sh "docker compose up -d --force-recreate"
        }
      }
    }

    stage('Verify Deployment') {
      steps {
        script {
          def retries = 20
          def sleepTime = 5
          def healthCheckPassed = false

          for (int i = 1; i <= retries; i++) {
            echo "Attempt $i/$retries - Checking application health..."

            try {
              def response = sh(script: "curl -s http://192.168.1.100:9001/actuator/health", returnStdout: true).trim()
              echo "Health check response: ${response}"

              if (response.contains('\"status\":\"UP\"')) {
                echo "Application is healthy!"
                healthCheckPassed = true
                break
              } else {
                echo "Status not UP yet, waiting..."
              }
            } catch (err) {
              echo "Curl failed on attempt $i: ${err}"
            }

            sleep(sleepTime)
          }

          if (!healthCheckPassed) {
            error("Application health check failed after ${retries * sleepTime} seconds.")
          }
        }
      }
    }

    post {
        success {
          sendDiscord("Pipeline *SUCCESSFUL* for branch `${env.BRANCH_NAME}`")
        }
        failure {
          sendDiscord("Pipeline *FAILED* for branch `${env.BRANCH_NAME}`")
        }
        always {
          echo 'Pipeline finished.'
        }
      }
    }
  }

// These helper methods are scoped for `when` conditions
def isBuildOrPR(String branch) {
  return branch ==~ /main|develop/ || branch?.startsWith("feature/") || env.CHANGE_ID
}

def isFullDeployBranch(String branch) {
  return branch ==~ /main|develop/ || branch?.startsWith("feature/")
}

def notifyDiscord(String message) {
  withCredentials([string(credentialsId: 'discord-webhook', variable: 'DISCORD_WEBHOOK')]) {
    sh """
      curl -H "Content-Type: application/json" \
           -X POST \
           -d '{ "content": "${message}" }' \
           $DISCORD_WEBHOOK
    """
  }
}