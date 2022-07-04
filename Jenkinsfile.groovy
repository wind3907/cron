pipeline {
    agent { label 'master' }
    parameters {
        string(name: 'TEST', defaultValue: '001')
    }
    environment {
        TEST="${params.TEST}"
    }
    stages {
        stage('Checkout SCM') {
            steps {
                cleanWs()
                checkout scm
                echo "Building ${env.JOB_NAME}..."
            }
        }
    }
    post {
        success {
            script {
                echo 'Success'
            }
        }
        failure {
            script {
                echo 'Failure'
            }
        }
    }
}

