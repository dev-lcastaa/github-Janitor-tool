pipeline {
  agent any

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

    stage('Building Docker Images') {
      when {
        expression {
          return isFullDeployBranch(env.BRANCH_NAME)
        }
      }
      steps {
        echo "Building docker images"
      }
    }

    stage('Publishing Docker Images') {
      when {
        expression {
          return isFullDeployBranch(env.BRANCH_NAME)
        }
      }
      steps {
        echo "Publishing docker images"
      }
    }

    stage('Deploying docker images') {
      when {
        expression {
          return isFullDeployBranch(env.BRANCH_NAME)
        }
      }
      steps {
        echo "Deploying docker images"
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