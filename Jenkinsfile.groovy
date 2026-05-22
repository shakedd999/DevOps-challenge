pipeline {
    agent any

    stages {
        stage('Build-Image') {
            steps {
                bat 'docker build -t shakeddaniel/devops-challenge:%BUILD_NUMBER% .'
            }
        }
        stage('Docker-login') {
            steps {
                bat 'docker login -u shakeddaniel -p $DOCKER_PASSWORD'
            }
        }
        stage('Push-Image') {
            steps {
                bat 'docker push shakeddaniel/devops-challenge:%BUILD_NUMBER%'
            }
        }
        stage('change-image-tag') {
            steps {
                powershell '''
                    (Get-Content devops-challenge-chart/values.yaml) -replace 'tag: "1.0.0"', "tag: `"$env:BUILD_NUMBER`"" | Set-Content devops-challenge-chart/values.yaml
                '''
            }
        }
        stage('commitchanges') {
            steps {
                bat '''
                    git add devops-challenge-chart/values.yaml
                    git commit -m "Update image tag to $BUILD_NUMBER"
                    git push
                '''
            }
        }
    }
}