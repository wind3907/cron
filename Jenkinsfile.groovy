node {
    checkout scm
    env.WORKSPACE = pwd()
    env.SCHEDULE = readFile "${WORKSPACE}/data_migration_schedule.txt"
}

properties(
    [
        buildDiscarder(logRotator(numToKeepStr: '20')),
        parameters(
            [
                separator(name: "data-migration", sectionHeader: "Data Migration Parameters"),
                string(name: 'SOURCE_DB', defaultValue: 'rsxxxe', description: 'Source Database. eg: rs040e'),
                string(name: 'TARGET_DB', defaultValue: 'lx###trn', description: 'Target Database. eg: lx036trn'),
                string(name: 'ROOT_PW', defaultValue: '', description: 'Root Password'),
                string(name: 'TARGET_SERVER', defaultValue: 'lx###trn', description: 'Host ec2 instance. eg: lx036trn'),
                separator(name: "deployment", sectionHeader: "Deployment Parameters"),
                [
                    name: 'artifact_s3_bucket',
                    description: 'The build\'s targeted platform',
                    $class: 'ChoiceParameter',
                    choiceType: 'PT_SINGLE_SELECT',
                    filterLength: 1,
                    filterable: false,
                    randomName: 'choice-parameter-289390844205293',
                    script: [
                        $class: 'GroovyScript',
                        script: [classpath: [], sandbox: false, script: '''\
                            return [
                                \'swms-build-artifacts\',
                                \'swms-build-dev-artifacts\'
                            ]'''.stripIndent()
                        ]
                    ]
                ],
                [
                    name: 'platform',
                    description: 'The build\'s targeted platform',
                    $class: 'ChoiceParameter',
                    choiceType: 'PT_SINGLE_SELECT',
                    filterLength: 1,
                    filterable: false,
                    randomName: 'choice-parameter-289390844205293',
                    script: [
                        $class: 'GroovyScript',
                        script: [classpath: [], sandbox: false, script: '''\
                            return [
                                \'linux\',
                                \'aix_11g_11g\',
                                \'aix_19c_12c\'
                            ]'''.stripIndent()
                        ]
                    ]
                ],
                string(name: 'artifact_version', defaultValue: '50_0', description: 'The swms version to deploy', trim: true),
                [
                    name: 'artifact_name',
                    description: 'The name of the artifact to deploy',
                    $class: 'CascadeChoiceParameter',
                    choiceType: 'PT_SINGLE_SELECT',
                    filterLength: 1,
                    filterable: false,
                    randomName: 'choice-parameter-artifact_name',
                    referencedParameters: 'artifact_s3_bucket, platform, artifact_version',
                    script: [
                        $class: 'GroovyScript',
                        script: [classpath: [], sandbox: false, script: '''\
                                if (platform?.trim() && artifact_version?.trim()) {
                                    def process = "aws s3api list-objects --bucket ${artifact_s3_bucket} --prefix ${platform}-${artifact_version} --query Contents[].Key".execute()
                                    return process.text.replaceAll('"', "").replaceAll("\\n","").replaceAll(" ","").tokenize(',[]')
                                } else {
                                    return []
                                }
                            '''.stripIndent()
                        ]
                    ]
                ],
                string(name: 'dba_masterfile_names', defaultValue: 'R50_0_dba_master.sql', description: 'Comma seperated names of the Privileged master files to apply to the current database. Will not run if left blank. Ran before the master_file', trim: true),
                string(name: 'master_file_retry_count', description: 'Amount of attempts to apply the master file. This is setup to handle circular dependencies by running the same master file multiple times.', defaultValue: '3', trim: true)
            ]
        )
    ]
)

pipeline {
    agent { label 'master' }
    triggers {
        parameterizedCron(SCHEDULE)
    }
    environment {
        TARGET_DB = "${params.TARGET_DB}"
        SOURCE_DB = "${params.SOURCE_DB}"
    }
    stages {
        stage('Biweekly Configuration') {
            steps {
                script {
                    def STATUS = true
                    echo "STATUS: ${STATUS}"
                    if( STATUS == true ){
                        env.TRIGGER = 'false'
                        sh(script: '''echo 'false' | aws s3 cp - s3://swms-scheduled-data-migration/${TARGET_DB}/status''')
                    }else{
                        echo "else block"
                        env.TRIGGER = 'true'
                        sh(script: '''echo 'true' | aws s3 cp - s3://swms-scheduled-data-migration/${TARGET_DB}/status''')
                    }
                }
            }
        }
        stage('Triiger') {
            when { environment name: 'TRIGGER', value: 'true' }
            steps {
                script{
                    echo "This pipeline is executed ${TARGET_DB}"
                }
            }
        }
    }
}