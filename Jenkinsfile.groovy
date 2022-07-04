def data = '''
            50 * * * * %GREETING=Hola;PLANET=Pluto
        '''
def workspace = "${WORKSPACE}"
properties(
    [
        buildDiscarder(logRotator(numToKeepStr: '20')),
        parameters(
            [
                string(name: 'PLANET', defaultValue: 'Earth'),
                string(name: 'GREETING', defaultValue: 'Hello'),
            ]
        ),
    ]
)
pipeline {
    agent { label 'master' }
    environment {
        TEST="${params.TEST}"
    }
    triggers {
        parameterizedCron(data)
    }
    stages {
        stage('Checkout SCM') {
            steps {
                cleanWs()
                checkout scm
                echo "Building ${env.JOB_NAME}..."
            }
        }
        stage('Info') {
            steps {
                echo "${params.PLANET}"
                echo "${params.GREETING}"
                echo "${workspace}"
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

