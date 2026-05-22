pipeline {
    agent any

    stages {
        stage('Clone-Repository') {
            steps {
                git branch: 'main', url: 'https://github.com/shakedd999/DevOps-challenge.git'
            }
        }

        stage('SAST-Scan') {
            steps {
                sh '''
                    semgrep scan --config auto --error --severity ERROR .
                '''
            }
        }

        stage('Build-Image') {
            steps {
                sh 'docker build -t shakeddaniel/devops-challenge:${BUILD_NUMBER} .'
            }
        }

        stage('Scan-Docker-Image') {
            steps {
                sh '''
                    trivy image --exit-code 1 --severity CRITICAL shakeddaniel/devops-challenge:${BUILD_NUMBER}
                '''
            }
        }

        stage('Docker-Login') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                }
            }
        }

        stage('Push-Image') {
            steps {
                sh 'docker push shakeddaniel/devops-challenge:${BUILD_NUMBER}'
            }
        }

        stage('Change-Image-Tag') {
            steps {
                sh '''
                    sed -i 's/tag: ".*"/tag: "'"${BUILD_NUMBER}"'"/' devops-challenge-chart/values.yaml
                '''
            }
        }

        stage('Commit-Changes') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'github-creds',
                    usernameVariable: 'GIT_USER',
                    passwordVariable: 'GIT_TOKEN'
                )]) {
                    sh '''
                        git config user.email "jenkins@example.com"
                        git config user.name "jenkins"

                        git add devops-challenge-chart/values.yaml
                        git commit -m "Update image tag to ${BUILD_NUMBER}" || echo "No changes to commit"

                        git push https://${GIT_USER}:${GIT_TOKEN}@github.com/shakedd999/DevOps-challenge.git HEAD:main
                    '''
                }
            }
        }
    }
}