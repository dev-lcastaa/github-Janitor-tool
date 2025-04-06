pipeline {

  agent { label 'jenkins-aql-node-3' }

  environment {
     IMAGE_NAME = 'lcastaa/git-hub-scm'
     IMAGE_TAG = 'latest'
     DOCKER_CREDENTIALS_ID = 'docker-login'
  }

  stages {

    stage('Build') {
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
         withCredentials([usernamePassword(credentialsId: 'docker-login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
           sh """
              echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
              docker compose pull
           """
         }
         sh "docker compose up -d --force-recreate"
      }
    }

    stage('Verify Deployment') {
     steps {
       script {
         def retries = 10
         def healthCheckPassed = false
         for (int i = 0; i < retries; i++) {
           echo "Checking Application health...."
           def response = sh(script: "curl -s http://192.168.0.100:9001/actuator/health", returnStdout: true).trim()
             if (response.contains('"status":"UP"')) {
               echo "Application is healthy!"
               healthCheckPassed = true
               break
             } else {
               echo "Waiting for app to become healthy... ($i/${retries})"
               sleep(5)
               }
             }
             if (!healthCheckPassed) {
               error("Application health check failed after ${retries} attempts.")
             }
           }
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