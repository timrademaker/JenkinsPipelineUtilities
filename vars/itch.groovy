class ItchConfiguration {
    static String butlerExe = '';
    static String user = '';
    static String apiKeyID = '';
}


def init(String butlerExecutable, String apiKeyID = '', String username = '') {
    assert(file.exists(butlerExecutable));

    ItchConfiguration.butlerExe = butlerExecutable;
    ItchConfiguration.apiKeyID = apiKeyID;
    ItchConfiguration.user = username;
}

def push(String gameDirectory, String gameName, String channel, Boolean dryRun = false, List<String> ignoredFiles = []) {
    assert(file.dirExists(gameDirectory));
    assert(gameName);
    assert(channel);

    String ignoreStr = '';
    for(file in ignoredFiles) {
        ignoreStr += "--ignore '${file}'";
    }
    
    String dryRunStr = '';
    if(dryRun) {
        dryRunStr = '--dry-run';
    }

    withCredentials([string(credentialsId: ItchConfiguration.apiKeyID, variable: 'BUTLER_API_KEY')]) {
        bat label: 'Upload build to itch.io', script: """${config.butlerExe} login
                        ${ItchConfiguration.butlerExe} push --if-changed --assume-yes ${ignoreStr} ${dryRunStr} '${gameDirectory}' '${ItchConfiguration.username}/${gameName}:${channel}'
                        ${ItchConfiguration.butlerExe} logout --assume-yes"""
    }
}
