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
        GIT_REPO = "iqtree2"
        BUILD_SCRIPTS = "${WORKING_DIR}/build-scripts"
        IQTREE_DIR = "${WORKING_DIR}/${GIT_REPO}"

        // build directories
        /*

            1. build-mpi --> build the mpi version of iqtree2
            2. build-wompi --> build the non-mpi + openmp version of iqtree2
            3. build-nn --> build the non-mpi + openmp + NN version of iqtree2
            4. build-nn-mpi --> build the mpi + NN version of iqtree2
            4. build-gpu-nn --> build the non-mpi (openmp) + openmp + NN + GPU version of iqtree2
            6. build-gpu-nn-mpi --> build the mpi + NN + GPU version of iqtree2
         */
        BUILD_MPI = "${WORKING_DIR}/build-mpi"
        BUILD_WOMPI = "${WORKING_DIR}/build-wompi"
        BUILD_NN = "${WORKING_DIR}/build-nn"
        BUILD_NN_MPI = "${WORKING_DIR}/build-nn-mpi"
        BUILD_GPU_NN = "${WORKING_DIR}/build-gpu-nn"
        BUILD_GPU_NN_MPI = "${WORKING_DIR}/build-gpu-nn-mpi"


    }
    stages {
    // ssh to NCI_ALIAS and scp build-scripts to working dir in NCI
        stage('Copy build scripts') {
            steps {
                script {
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
                        git clone --recursive ${IQTREE_GIT_URL}
                        cd ${GIT_REPO}
                        git checkout ${params.BRANCH}
                        exit
                        EOF
                        """
                }
            }
        }
        stage("Build") {
            steps {
                script {
                    sh """
                        ssh ${NCI_ALIAS} << EOF
                        mkdir -p ${WORKING_DIR}/build
                        cd ${WORKING_DIR}/build
                      
                        echo "building mpi version"
                        
                        sh ${BUILD_SCRIPTS}/jenkins-cmake-build-mpi.sh ${BUILD_MPI} ${IQTREE_DIR}
                          
                        
                        exit
                        EOF
                        """
                }
            }
        }

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

def void cleanWs() {
    // ssh to NCI_ALIAS and remove the working directory
    sh "ssh ${NCI_ALIAS} 'rm -rf ${IQTREE_DIR} ${BUILD_SCRIPTS}'"
}