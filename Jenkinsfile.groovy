properties(
    [
        buildDiscarder(logRotator(numToKeepStr: '20')),
        parameters(
            [
                string(name: 'PLANET', defaultValue: 'Earth'),
                string(name: 'GREETING', defaultValue: 'Hello'),
            ]
        ),
        triggers(
            parameterizedCron('''
            */2 * * * * %GREETING=Hola;PLANET=Pluto
            */3 * * * * %PLANET=Mars
            */5 * * * *
        ''')
        )
    ]
)
pipeline {
    agent { label 'master' }
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
        stage('Info') {
            steps {
                echo "${params.PLANET}"
                echo "${params.GREETING}"
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

