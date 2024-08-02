//  a JenkinsFile to build iqtree
// paramters
//  1. git branch
// 2. git url


properties([
    parameters([
        string(name: 'BRANCH', defaultValue: 'master', description: 'Branch to build'),
    ])
])
pipeline {
    agent any
    environment {
        IQTREE_GIT_URL = "https://github.com/iqtree/iqtree2.git"
        NCI_ALIAS = "nci_gadi"
        WORKING_DIR = "/scratch/dx61/sa0557/iqtree2/ci-cd"

    }
    stages {
    // ssh to NCI_ALIAS and scp build-scripts to working dir in NCI
        stage('Copy build scripts') {
            steps {
                script {
//                    sh "mkdir -p ${WORKING_DIR}"
//                    sh "cp -r build-scripts ${WORKING_DIR}"
                    sh "pwd"
                    sh "scp -r build-scripts ${NCI_ALIAS}:${WORKING_DIR}"
                }
            }
        }
        stage('Setup environment') {
            steps {
                script {
                    sh """
                        ssh ${NCI_ALIAS} << EOF
                        mkdir -p ${WORKING_DIR}
                        cd  ${WORKING_DIR}
                        git clone ${IQTREE_GIT_URL} .
                        git checkout ${params.BRANCH}
                        exit
                        EOF
                        """
                }
            }
        }
//        stage('Copy build scripts') {
//            steps {
//                script {
////                    sh "mkdir -p ${WORKING_DIR}"
////                    sh "cp -r build-scripts ${WORKING_DIR}"
//                    sh "scp -r build-scripts ${NCI_ALIAS}:${WORKING_DIR}"
//                }
//            }
//        }
//        stage('Run') {
//            steps {
//                script {
//                    sh "ssh ${NCI_ALIAS} 'cd ${WORKING_DIR}/build-scripts && ./build.sh'"
//                }
//            }
//        }
        stage ('Verify') {
            steps {
                script {
                    sh "ssh ${NCI_ALIAS} 'cd ${WORKING_DIR} && ls -l'"
                }
            }
        }


    }
    post {
        always {
            echo 'Cleaning up workspace'
            cleanWs()
        }
    }
}