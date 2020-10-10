def upload(String sentryCLIPath, String authTokenCreds, String organisation, String project, String outputFolder)
{
    assert(file.exists(sentryCLIPath));
    
    withCredentials([string(credentialsId: authTokenCreds, variable: 'SENTRY_AUTH_TOKEN')]) {
        bat(label: "Upload debug symbols to Sentry", script: "\"${sentryCLIPath}\" --auth-token ${SENTRY_AUTH_TOKEN} upload-dif -o \"${organisation}\" -p \"${project}\" \"${outputFolder}\"")
    }
}
