pipeline {
  agent { label 'jenkins-aql-node-3' }

  environment {
    IMAGE_NAME = 'lcastaa/git-hub-scm'
    IMAGE_TAG = 'latest'
    DOCKER_CREDENTIALS_ID = 'docker-login'
    GITHUB_API_KEY = credentials('GITHUB_API_KEY')
    DISCORD_REPORT_NOTIFICATION = credentials('DISCORD_REPORT_NOTIFICATION')
  }

  stages {
    stage('Pipeline Begins') {
      when {
        expression { isBuildOrPR(env.BRANCH_NAME) }
      }
      steps {
        script {
          notifyDiscord("━━ Started *Aql-SCM-Hygiene-Tool* pipeline for branch `${env.BRANCH_NAME}` ━━")
        }
      }
    }

    stage('Build') {
      when {
        expression { isBuildOrPR(env.BRANCH_NAME) }
      }
      steps {
        script {
          notifyDiscord("├─ Executing Build Stage....")
        }
        echo "Building branch: ${env.BRANCH_NAME}"
        sh "./mvnw clean compile -Dsweeper.api.key=$GITHUB_API_KEY -Dsweeper.discord.notify.endpoint=$DISCORD_REPORT_NOTIFICATION"
      }
    }

    stage('Test') {
      when {
        expression { isBuildOrPR(env.BRANCH_NAME) }
      }
      steps {
        script {
          notifyDiscord("├─ Running Application Tests Stage....")
        }
        echo "Testing branch: ${env.BRANCH_NAME}"
        sh "./mvnw test -Dsweeper.api.key=$GITHUB_API_KEY -Dsweeper.discord.notify.endpoint=$DISCORD_REPORT_NOTIFICATION"
      }
    }

    stage('Post-Test Tasks') {
      when {
        expression { isBuildOrPR(env.BRANCH_NAME) }
      }
      steps {
        script {
          notifyDiscord("├─ Running Post-Test Tasks Stage....")
        }
        echo "Publishing test results"
      }
    }

    stage('Packaging Executable JAR') {
      when {
        expression { isBuildOrPR(env.BRANCH_NAME) }
      }
      steps {
        script {
          notifyDiscord("├─ Packaging JAR Stage....")
        }
        sh "./mvnw clean package -DskipTests -Dsweeper.api.key=$GITHUB_API_KEY -Dsweeper.discord.notify.endpoint=$DISCORD_REPORT_NOTIFICATION"
      }
    }

    stage('Building Docker Image') {
      when {
        expression { isFullDeployBranch(env.BRANCH_NAME) }
      }
      steps {
        script {
          notifyDiscord("├─ Building Docker Image Stage....")
        }
        sh "docker build --build-arg GITHUB_API_KEY=$GITHUB_API_KEY --build-arg DISCORD_REPORT_NOTIFICATION=$DISCORD_REPORT_NOTIFICATION -t ${IMAGE_NAME}:${IMAGE_TAG} ."
      }
    }

    stage('Publishing Docker Image') {
      when {
        expression { isFullDeployBranch(env.BRANCH_NAME) }
      }
      steps {
        script {
          notifyDiscord("├─ Publishing Docker Image Stage....")
        }
        withCredentials([usernamePassword(credentialsId: DOCKER_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh """
            echo "\${DOCKER_PASS}" | docker login -u "\${DOCKER_USER}" --password-stdin
            docker push \${IMAGE_NAME}:\${IMAGE_TAG}
            docker logout
          """
        }
      }
    }

    stage('Deploying Docker Image') {
      when {
        expression { isFullDeployBranch(env.BRANCH_NAME) }
      }
      steps {
        script {
          notifyDiscord("├─ Deploying Docker Image Stage....")

          sh "chmod +x ./pipeline-scripts/clean-containers.sh"
          def cleanupStatus = sh(script: "./pipeline-scripts/clean-containers.sh janitor-tool", returnStatus: true)
          if (cleanupStatus != 0) {
            error("Cleanup script failed with exit code ${cleanupStatus}. Stopping deployment.")
          }

          withCredentials([usernamePassword(credentialsId: DOCKER_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
            sh """
              echo "\${DOCKER_PASS}" | docker login -u "\${DOCKER_USER}" --password-stdin
              docker compose pull
              docker compose up -d --force-recreate
              docker logout
            """
          }
        }
      }
    }

    stage('Verify Deployment') {
      when {
        expression { isFullDeployBranch(env.BRANCH_NAME) }
      }
      steps {
        script {
          notifyDiscord("├─ Verifying Deployment Stage....")
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
  }

  post {
    success {
      script {
        sendDiscord("└─ Pipeline *SUCCESSFUL*")
      }
    }
    failure {
      script {
        sendDiscord("└─ Pipeline *FAILED* ")
      }
    }
    always {
      echo 'Pipeline finished.'
    }
  }
}

// === HELPER FUNCTIONS ===

def isBuildOrPR(String branch) {
  return branch ==~ /main|develop/ || branch?.startsWith("feature/") || env.CHANGE_ID
}

def isFullDeployBranch(String branch) {
  return branch ==~ /main|develop/
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

def sendDiscord(String message) {
  notifyDiscord(message)
}
