pipeline {
    agent { label 'master' }
    triggers {
        parameterizedCron('''
            # leave spaces where you want them around the parameters. They'll be trimmed.
            # we let the build run with the default name
            */2 * * * * %GREETING=Hola;PLANET=Pluto
            */3 * * * * %PLANET=Mars
        ''')
    }
    stages {
        stage('Test') {
            steps {
                echo 'hi'
            }
        }
        // stage("SWMS Data Migration") {
        //     steps {
        //         echo "Section: SWMS Data Migration"
        //         script {
        //             try {
        //                 build job: "swms-db-migrate-AIX-RDS-test", parameters: [
        //                     string(name: 'SOURCE_DB', value: "${params.SOURCE_DB}"),
        //                     string(name: 'TARGET_DB', value: "${params.TARGET_DB}"),
        //                     string(name: 'ROOT_PW', value: ""),
        //                     string(name: 'TARGET_SERVER', value: "${params.TARGET_SERVER}"),
        //                     string(name: 'artifact_s3_bucket', value: "${params.artifact_s3_bucket}"),
        //                     string(name: 'platform', value: "${params.platform}"),
        //                     string(name: 'artifact_version', value: "${params.artifact_version}"),
        //                     string(name: 'artifact_name', value: "${params.artifact_name}"),
        //                     string(name: 'dba_masterfile_names', value: "${params.dba_masterfile_names}"),
        //                     string(name: 'master_file_retry_count', value: "${params.master_file_retry_count}")
        //                 ]
        //                 echo "Data Migration Successful!"
        //             } catch (e) {
        //                 echo "Data Migration Failed!"
        //                 throw e
        //             }
        //         }
        //     }
        // }  
    }
    post {
        success {
            script {
                echo 'Scheduled Data Migration is completed'
            }
        }
        failure {
            script {
                echo 'Scheduled Data Migration is Failed'
            }
        }
    }
}

