class ItchConfiguration implements Serializable {
    static String butlerExe = '';
    static String user = '';
    static String apiKeyID = '';
}


def init(String butlerExecutable, String apiKeyID, String username) {
    ensureButlerExecutableExists(butlerExecutable, true);

    ItchConfiguration.butlerExe = butlerExecutable;
    ItchConfiguration.apiKeyID = apiKeyID;
    ItchConfiguration.user = username;
}

def push(String gameDirectory, String gameName, String channel, Boolean dryRun = false, List<String> ignoredFiles = []) {
    ensureButlerExecutableExists(ItchConfiguration.butlerExe);
    if(!file.dirExists(gameDirectory)) {
        failStage("Game directory not found! (${gameDirectory})");
    }

    if(gameName == '') {
        failStage('No game name provided!');
    }

    if(gameName == '') {
        failStage('No channel name provided!');
    }

    String ignoreStr = '';
    for(file in ignoredFiles) {
        ignoreStr += "--ignore \"${file}\" ";
    }

    withCredentials([string(credentialsId: ItchConfiguration.apiKeyID, variable: 'BUTLER_API_KEY')]) {
        bat label: 'Upload build to itch.io', script: """${ItchConfiguration.butlerExe} login
                        ${ItchConfiguration.butlerExe} push --if-changed --assume-yes ${ignoreStr} ${dryRun ? '--dry-run' : ''} "${gameDirectory}" "${ItchConfiguration.user}/${gameName}:${channel}"
                        ${ItchConfiguration.butlerExe} logout --assume-yes"""
    }
}

private def ensureButlerExecutableExists(String butlerPath, Boolean calledFromInit = false) {
    if(!file.exists(butlerPath)) {
        failStage("Butler executable not found at specified path! (${butlerPath})${calledFromInit ? '' : '\nDid you set it using itch.init?'}");
    }
}
