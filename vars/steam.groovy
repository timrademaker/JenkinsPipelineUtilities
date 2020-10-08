class SteamConfig {
    static String steamcmdExe = "/steamcmd/steamcmd.exe"
}

def setup(String steamcmdFolder = "${env.WORKSPACE}/steamcmd") {
    if(!file.dirExists(steamcmdFolder)) {
        file.createDir("${env.WORKSPACE}/temp");
        def outputFile = "${env.WORKSPACE}/temp/steamcmd.zip";

        powershell label: 'Download SteamCMD', script: "Invoke-Webrequest -URI http://media.steampowered.com/installer/steamcmd.zip -OutFile \"${outputFile}\""
        powershell label: 'Unzip SteamCMD', script: "Expand-Archive -Path '${env.WORKSPACE}/temp/steamcmd.zip' -DestinationPath '${steamcmdFolder}'"
        
        file.delete(outputFile);

        SteamConfig.steamcmdExe = "${steamcmdFolder}/steamcmd.exe";
    }
}

def createAppManifest(String appID, String depotID, String contentRoot, String buildDescription = '', Boolean isPreview = false, String localContentServerPath = '', String setLiveOnBranch = '', String buildOutputFolder = '') {
    def appManifest = libraryResource 'com/timrademaker/app_build_template.vdf'
    appManifest = appManifest.replace('<APP_ID>', appID);
    appManifest = appManifest.replace('<DEPOT_ID>', depotID);
    appManifest = appManifest.replace('<CONTENT_ROOT>', contentRoot);
    appManifest = appManifest.replace('<BUILD_DESCRIPTION>', "${buildDescription ? buildDescription : 'No description'}");
    appManifest = appManifest.replace('<IS_PREVIEW>', "${isPreview ? '1' : '0'}");
    appManifest = appManifest.replace('<LOCAL_CONTENT_SERVER_PATH>', localContentServerPath);
    appManifest = appManifest.replace('<SET_LIVE_ON_BRANCH>', setLiveOnBranch);
    appManifest = appManifest.replace('<BUILD_OUTPUT_FOLDER>', buildOutputFolder);

    file.write("app_build_${appID}.vdf", appManifest);
    return "app_build_${appID}.vdf";
}

def createDepotManifest(String depotID, String contentRoot, String localPath = '*', String depotPath = '.', Boolean addContentRecursively = true, String excludes = '*.pdb') {
    def depotManifest = libraryResource 'com/timrademaker/depot_build_template.vdf'
    depotManifest = depotManifest.replace('<DEPOT_ID>', depotID);
    depotManifest = depotManifest.replace('<CONTENT_ROOT>', contentRoot);
    depotManifest = depotManifest.replace('<LOCAL_PATH>', localPath);
    depotManifest = depotManifest.replace('<DEPOT_PATH>', depotPath);
    depotManifest = depotManifest.replace('<SHOULD_ADD_CONTENT_RECURSIVELY>', "${addContentRecursively ? '1' : '0'}");
    depotManifest = depotManifest.replace('<EXCLUDED_FILES>', excludes);

    file.write("depot_build_${depotID}.vdf", depotManifest);
    return "depot_build_${depotID}.vdf";
}

def tryDeploy(String steamCredentials, String appManifest) {
    def result = deploy(steamCredentials, appManifest);

    def attempts = 0;
    while((result & (SteamResult.needsGuardCode | SteamResult.guardCodeMismatch)) != 0 && attempts < 3) {
        def guardCode = input message: 'Enter Steam Guard code', ok: 'Submit', 
            parameters: [
                string(defaultValue: '', description: 'The Steam Guard code needed to log in to the account used by this pipeline.', name: 'Steam Guard Code', trim: true)
            ]
        
        result = deploy(steamCredentials, appManifest, guardCode);
        
        ++attempts;
    }
}

private def deploy(String steamCredentials, String appManifest, String steamGuardCode = '') {
    def output = '';
    withCredentials([usernamePassword(credentialsId: "${steamCredentials}", passwordVariable: 'STEAM_PASS', usernameVariable: 'STEAM_USER')]) {
        output = bat label: 'Steam build', returnStdout: true, script: "\"${SteamConfig.steamcmdExe}\" +login \"${env.STEAM_USER}\" \"${env.STEAM_PASS}\" ${steamGuardCode} +run_app_build \"${appManifest}\" +quit"
    }

    if(output.contains('need two-factor code')) {
        log.error('Unable to log into SteamCMD. 2FA code is required.');
        return SteamResult.needsGuardCode;
    } else if(output.contains('Invalid Password')) {
        log.error('Unable to log into SteamCMD. Invalid password provided.');
        return SteamResult.invalidPassword;
    } else if(output.contains('Two-factor code mismatch')) {
        log.error('Unable to log into SteamCMD. 2FA code mismatch.');
        return SteamResult.guardCodeMismatch;
    }

    return SteamResult.success;
}

class SteamResult {
    static final int success = 0;
    static final int invalidPassword = 1 << 1;
    static final int needsGuardCode = 1 << 2;
    static final int guardCodeMismatch = 1 << 3;
}
