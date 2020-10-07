class ItchConfiguration {
    String butlerExe = '';
    String user = '';
    String game = '';
    String channel = '';
    String apiKeyID = '';
    List<String> ignoredFiles = [];
}

def itchConfig = new ItchConfiguration();

def init(String butlerExecutable, String apiKeyID = '', String username = '') {
    assert(file.exists(butlerExecutable));

    itchConfig.butlerExe = butlerExecutable;
    itchConfig.apiKeyID = apiKeyID;
    itchConfig.user = username;
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

    withCredentials([string(credentialsId: itchConfig.apiKeyID, variable: 'BUTLER_API_KEY')]) {
        bat label: 'Upload build to itch.io', script: """${config.butlerExe} login
                        ${itchConfig.butlerExe} push --if-changed --assume-yes ${ignoreStr} ${dryRunStr} '${gameDirectory}' '${itchConfig.username}/${gameName}:${channel}'
                        ${itchConfig.butlerExe} logout --assume-yes"""
    }
}
