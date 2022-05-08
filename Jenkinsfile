pipeline {
  agent any
  stages {
    stage("build"){
      steps {
        echo "building app..."
      }
    }
    stage("test"){
      steps {
        echo "testing app..."
        sh "mvn test"
      }
    }
    stage("deploy"){
      steps {
        echo "deploy app..."
      }
    }
  }
}
