//  a JenkinsFile to build iqtree
// paramters
//  1. git branch
// 2. git url

pipeline {
    agent any
    parameters {
        string(name: 'BRANCH', defaultValue: 'master', description: 'Branch to build')
        string(name: 'NCI_ALIAS', defaultValue: 'nci_gadi', description: 'ssh alias, if you do not have one, create one')

        string(name: 'WORKING_DIR', defaultValue: '/scratch/dx61/sa0557/iqtree2/ci-cd', description: 'Working directory')

        // bool for building NN
        booleanParam(defaultValue: true, description: 'Run the NN?', name: 'NN')
        string(name: 'ONNX_NN', description: 'onnxruntime for NN (use 1.12 version)', defaultValue: '/scratch/dx61/sa0557/iqtree2/onnxruntime-linux-x64-1.12.1')

        booleanParam(defaultValue: true, description: 'Run the GPU?', name: 'GPU')
        string(name: 'ONNX_NN_GPU', description: 'onnxruntime for NN-CUDA (use 1.12 version)', defaultValue: '/scratch/dx61/sa0557/iqtree2/onnxruntime-linux-x64-gpu-1.12.1')

    }
    environment {
        IQTREE_GIT_URL = "https://github.com/iqtree/iqtree2.git"
        NCI_ALIAS = "${params.NCI_ALIAS}"
        WORKING_DIR = "${params.WORKING_DIR}"
        GIT_REPO = "iqtree2"
        BUILD_SCRIPTS = "${WORKING_DIR}/build-scripts"
        IQTREE_DIR = "${WORKING_DIR}/${GIT_REPO}"
        BUILD_OUTPUT_DIR = "${WORKING_DIR}/builds"

        // build directories
        /*

            1. build-mpi --> build the mpi version of iqtree2
            2. build-wompi --> build the non-mpi + openmp version of iqtree2
            3. build-nn --> build the non-mpi + openmp + NN version of iqtree2
            4. build-nn-mpi --> build the mpi + NN version of iqtree2
            4. build-gpu-nn --> build the non-mpi (openmp) + openmp + NN + GPU version of iqtree2
            6. build-gpu-nn-mpi --> build the mpi + NN + GPU version of iqtree2
         */
        BUILD_MPI = "${BUILD_OUTPUT_DIR}/build-mpi"
        BUILD_WOMPI = "${BUILD_OUTPUT_DIR}/build-wompi"
        BUILD_NN = "${BUILD_OUTPUT_DIR}/build-nn"
        BUILD_NN_MPI = "${BUILD_OUTPUT_DIR}/build-nn-mpi"
        BUILD_GPU_NN = "${BUILD_OUTPUT_DIR}/build-gpu-nn"
        BUILD_GPU_NN_MPI = "${BUILD_OUTPUT_DIR}/build-gpu-nn-mpi"


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
                        mkdir -p ${BUILD_OUTPUT_DIR}
                        mkdir -p ${BUILD_SCRIPTS}
                        cd ${BUILD_OUTPUT_DIR}
                        rm -rf *
                        exit
                        EOF
                        """

                    // create env.sh file if NN or GPU is enabled
                    if ("${params.NN}" || "${params.GPU}") {
                        def envFileContent = """
export ONNX_NN=${params.ONNX_NN}
export ONNX_NN_GPU=${params.ONNX_NN_GPU}
"""
                        writeFile file: "${BUILD_SCRIPTS}/env.sh", text: envFileContent
                    }

                }
            }
        }
        stage("Build: Build MPI") {
            steps {
                /*

                    1. build-mpi --> build the mpi version of iqtree2
                    2. build-wompi --> build the non-mpi + openmp version of iqtree2
                    3. build-nn --> build the non-mpi + openmp + NN version of iqtree2
                    4. build-nn-mpi --> build the mpi + NN version of iqtree2
                    4. build-gpu-nn --> build the non-mpi (openmp) + openmp + NN + GPU version of iqtree2
                    6. build-gpu-nn-mpi --> build the mpi + NN + GPU version of iqtree2
                 */
                script {
                    sh """
                        ssh ${NCI_ALIAS} << EOF

                                              
                        echo "building mpi version"                        
                        sh ${BUILD_SCRIPTS}/jenkins-cmake-build-mpi.sh ${BUILD_MPI} ${IQTREE_DIR}
                        
                       
                        exit
                        EOF
                        """
                }
            }
        }

        stage("Build: Build WOMPI") {
            steps {
                /*

                    1. build-mpi --> build the mpi version of iqtree2
                    2. build-wompi --> build the non-mpi + openmp version of iqtree2
                    3. build-nn --> build the non-mpi + openmp + NN version of iqtree2
                    4. build-nn-mpi --> build the mpi + NN version of iqtree2
                    4. build-gpu-nn --> build the non-mpi (openmp) + openmp + NN + GPU version of iqtree2
                    6. build-gpu-nn-mpi --> build the mpi + NN + GPU version of iqtree2
                 */
                script {
                    sh """
                        ssh ${NCI_ALIAS} << EOF

                        echo "building non-mpi + openmp version"
                        sh ${BUILD_SCRIPTS}/jenkins-cmake-build-wompi.sh ${BUILD_WOMPI} ${IQTREE_DIR}

                        exit
                        EOF
                        """
                }
            }
        }

        stage("Build: Build NN") {
            steps {
                // this stage only runs if NN is enabled
                script {
                    if ("${params.NN}") {
                        /*

                        1. build-mpi --> build the mpi version of iqtree2
                        2. build-wompi --> build the non-mpi + openmp version of iqtree2
                        3. build-nn --> build the non-mpi + openmp + NN version of iqtree2
                        4. build-nn-mpi --> build the mpi + NN version of iqtree2
                        4. build-gpu-nn --> build the non-mpi (openmp) + openmp + NN + GPU version of iqtree2
                        6. build-gpu-nn-mpi --> build the mpi + NN + GPU version of iqtree2
                     */

                        sh """
                        ssh ${NCI_ALIAS} << EOF

                        echo "building NN version"
                        sh ${BUILD_SCRIPTS}/jenkins-cmake-build-nn.sh ${BUILD_NN} ${IQTREE_DIR} ${BUILD_SCRIPTS}/env.sh

                        exit
                        EOF
                        """

                    } else {
                        echo "NN is disabled"
                    }
                }


            }
        }

        stage("Build: Build NN MPI") {
            steps {
                script {
                    // this stage only runs if NN is enabled
                    if ("${params.NN}") {
                        /*

                    1. build-mpi --> build the mpi version of iqtree2
                    2. build-wompi --> build the non-mpi + openmp version of iqtree2
                    3. build-nn --> build the non-mpi + openmp + NN version of iqtree2
                    4. build-nn-mpi --> build the mpi + NN version of iqtree2
                    4. build-gpu-nn --> build the non-mpi (openmp) + openmp + NN + GPU version of iqtree2
                    6. build-gpu-nn-mpi --> build the mpi + NN + GPU version of iqtree2
                 */
                        sh """
                        ssh ${NCI_ALIAS} << EOF

                        echo "building mpi + NN version"
                        sh ${BUILD_SCRIPTS}/jenkins-cmake-build-nn-mpi.sh ${BUILD_NN_MPI} ${IQTREE_DIR} ${BUILD_SCRIPTS}/env.sh

                        exit
                        EOF
                        """

                    } else {
                        echo "NN is disabled"
                    }
                }
            }
        }

        stage("Build: Build GPU NN") {
            // this stage only runs if GPU is enabled
            steps {
                script {
                    if ("${params.GPU}") {
                        /*

                    1. build-mpi --> build the mpi version of iqtree2
                    2. build-wompi --> build the non-mpi + openmp version of iqtree2
                    3. build-nn --> build the non-mpi + openmp + NN version of iqtree2
                    4. build-nn-mpi --> build the mpi + NN version of iqtree2
                    4. build-gpu-nn --> build the non-mpi (openmp) + openmp + NN + GPU version of iqtree2
                    6. build-gpu-nn-mpi --> build the mpi + NN + GPU version of iqtree2
                 */
                        sh """
                        ssh ${NCI_ALIAS} << EOF

                        echo "building non-mpi (openmp) + openmp + NN + GPU version"
                        sh ${BUILD_SCRIPTS}/jenkins-cmake-build-gpu-nn.sh ${BUILD_GPU_NN} ${IQTREE_DIR} ${BUILD_SCRIPTS}/env.sh
                        

                        exit
                        EOF
                        """

                    } else {
                        echo "GPU is disabled"
                    }
                }
            }
        }

        stage("Build: Build GPU NN MPI") {
            // this stage only runs if GPU is enabled
            steps {
                script {
                    if ("${params.GPU}") {
                        /*

                        1. build-mpi --> build the mpi version of iqtree2
                        2. build-wompi --> build the non-mpi + openmp version of iqtree2
                        3. build-nn --> build the non-mpi + openmp + NN version of iqtree2
                        4. build-nn-mpi --> build the mpi + NN version of iqtree2
                        4. build-gpu-nn --> build the non-mpi (openmp) + openmp + NN + GPU version of iqtree2
                        6. build-gpu-nn-mpi --> build the mpi + NN + GPU version of iqtree2
                     */
                        sh """
                        ssh ${NCI_ALIAS} << EOF

                        echo "building mpi + NN + GPU version"
                        sh ${BUILD_SCRIPTS}/jenkins-cmake-build-gpu-nn-mpi.sh ${BUILD_GPU_NN_MPI} ${IQTREE_DIR} ${BUILD_SCRIPTS}/env.sh
                        

                        exit
                        EOF
                        """

                    } else {
                        echo "GPU is disabled"
                    }
                }
            }
        }

        stage('Verify') {
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