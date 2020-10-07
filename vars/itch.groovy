class ItchConfig {
    String butlerExe = '';
    String user = '';
    String game = '';
    String channel = '';
    String apiKeyID = '';
    List<String> ignoredFiles = [];
}

def config = new ItchConfig();

def init(String butlerExecutable, String apiKeyID = '', String username = '', String gameName = '', String channel = '', List<String> ignoredFiles = []) {
    assert(file.exists(butlerExecutable));

    config.butlerExe = butlerExecutable;
    config.apiKeyID = apiKeyID;
    config.user = username;
    config.game = gameName;
    config.channel = channel;
    config.ignoredFiles = ignoredFiles;
}

def push(String gameDirectory, String apiKeyID = config.apiKeyID, String username = config.user, String gameName = config.game, String channel = config.channel, Bool dryRun = false, List<String> ignoredFiles = config.ignoredFiles) {
    assert(file.dirExists(gameDirectory));
    assert(apiKeyID);
    assert(username);
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

    withCredentials([string(credentialsId: apiKeyID, variable: 'BUTLER_API_KEY')]) {
        bat label: 'Upload build to itch.io', script: """${config.butlerExe} login
                        ${config.butlerExe} push --if-changed --assume-yes ${ignoreStr} ${dryRunStr} '${gameDirectory}' '${username}/${gameName}:${channel}'
                        ${config.butlerExe} logout --assume-yes"""
    }
}