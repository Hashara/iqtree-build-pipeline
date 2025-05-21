pipeline {
    agent any

    parameters {
        string(name: 'BRANCH', defaultValue: 'main', description: 'Git branch to build')
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: params.BRANCH, url: 'https://github.com/your-username/your-repo.git'
            }
        }

        stage('Build') {
            steps {
                echo "Building branch: ${params.BRANCH}"
                sh 'echo Hello, Jenkins!'
            }
        }
    }

    post {
        always {
            echo 'Pipeline completed.'
        }
    }
}
