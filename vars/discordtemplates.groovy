def sendSuccess(String webhookUrl) {
    discord.sendEmbed(
        webhookUrl,
        ':white_check_mark: BUILD SUCCEEDED :white_check_mark:',
        '',
        '65280', 
        [["${env.JOB_BASE_NAME} has succeeded", "Last Changelist: ${env.P4_CHANGELIST}"],
         ["Job url", "${env.BUILD_URL}"]],
        "${env.JOB_BASE_NAME} (${env.BUILD_NUMBER})"
    )
}

def sendFail(String webhookUrl) {
    discord.sendEmbed(
        webhookUrl,
        ':x: BUILD FAILED :x:',
        '',
        '16711680', 
        [
            ["${env.JOB_BASE_NAME} has failed", "Last Changelist: ${env.P4_CHANGELIST}"],
            ["Job url", "${env.BUILD_URL}"]
        ],
        "${env.JOB_BASE_NAME} (${env.BUILD_NUMBER})"
    )
}

def sendNewReview(String webhookUrl, String reviewID, String swarmUrl, String author, String description = 'None', String buildStatus = 'not built') {
    discord.sendEmbed(
        webhookUrl,
        ':warning: NEW REVIEW :warning:',
        description,
        '16776960',
        [
            ['A new review is ready', "${swarmUrl}/reviews/${id}"],
            ['Author', author],
            ['Participants', description],
            ['Build Status', buildStatus]
        ],
        "${env.JOB_BASE_NAME} (${env.BUILD_NUMBER})"
    )
}
