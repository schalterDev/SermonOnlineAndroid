pipeline {
  agent any
  stages {
    stage('Compile') {
      steps {
        sh './gradlew compileDebugSources'
      }
    }
    stage('Unit test') {
      steps {
        sh './gradlew testDebugUnitTest testDebugUnitTest'
        junit '**/TEST-*.xml'
      }
    }
    stage('Build APK') {
      steps {
        sh './gradlew assembleDebug'
      }
    }
    stage('Static analysis') {
      steps {
        sh './gradlew lintDebug'
        androidLint(pattern: '**/lint-results-*.xml')
      }
    }
    stage('Deploy') {
      steps {
        // Build the app in release mode, and sign the APK using the environment variables
        sh './gradlew assembleRelease'

        // Archive the APKs so that they can be downloaded from Jenkins
        archiveArtifacts '**/*.apk'
        step([$class: 'SignApksBuilder', apksToSign: '**/*-unsigned.apk', keyAlias: '', keyStoreId: 'sermon-online-cert']) 
      }
    }
  }
  options {
    skipStagesAfterUnstable()
  }
}
