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

      when {
        // Only execute this stage when building from the `firebase-admob-master` branch
        branch 'firebase-admob-master'
      }
      steps {
        sh './gradlew assembleRelease'
        signAndroidApks(archiveSignedApks: true, keyAlias: 'android-with-ads', keyStoreId: 'sermon-online-cert', apksToSign: '**/*-unsigned.apk')
      }
    }
  }
  options {
    skipStagesAfterUnstable()
  }
}
