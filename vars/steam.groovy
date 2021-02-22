class SteamConfig implements Serializable {
    static String steamcmdExe = "/steamcmd/steamcmd.exe"
}

def setup(String steamcmdFolder = "${env.WORKSPACE}/steamcmd") {
    if(!file.exists("${steamcmdFolder}/steamcmd.exe")) {
        file.createDir("${env.WORKSPACE}/temp");
        def outputFile = "${env.WORKSPACE}/temp/steamcmd.zip";

        file.download('http://media.steampowered.com/installer/steamcmd.zip', outputFile);
        file.unzip("${env.WORKSPACE}/temp/steamcmd.zip", steamcmdFolder);
        
        file.delete(outputFile);
    }

    SteamConfig.steamcmdExe = "${steamcmdFolder}/steamcmd.exe";

    bat label: 'Set Up SteamCMD', script: "\"${SteamConfig.steamcmdExe}\" +quit";
}

def createAppManifest(String appID, String depotID, String contentRoot, String buildDescription = 'No description', String setLiveOnBranch = '', String buildOutputFolder = '${env.WORKSPACE}/SteamBuild', Boolean isPreview = false, String localContentServerPath = '') {
    def appManifest = libraryResource 'com/timrademaker/app_build_template.vdf'
    appManifest = appManifest.replace('<APP_ID>', appID);
    appManifest = appManifest.replace('<DEPOT_ID>', depotID);
    appManifest = appManifest.replace('<CONTENT_ROOT>', contentRoot);
    appManifest = appManifest.replace('<BUILD_DESCRIPTION>', buildDescription);
    appManifest = appManifest.replace('<IS_PREVIEW>', "${isPreview ? '1' : '0'}");
    appManifest = appManifest.replace('<LOCAL_CONTENT_SERVER_PATH>', localContentServerPath);
    appManifest = appManifest.replace('<SET_LIVE_ON_BRANCH>', setLiveOnBranch);
    appManifest = appManifest.replace('<BUILD_OUTPUT_FOLDER>', buildOutputFolder);

    file.write("${env.WORKSPACE}/app_build_${appID}.vdf", appManifest);
    return "${env.WORKSPACE}/app_build_${appID}.vdf";
}

def createDepotManifest(String depotID, String contentRoot, String excludes = '*.pdb', String localPath = '*', String depotPath = '.', Boolean addContentRecursively = true) {
    def depotManifest = libraryResource 'com/timrademaker/depot_build_template.vdf'
    depotManifest = depotManifest.replace('<DEPOT_ID>', depotID);
    depotManifest = depotManifest.replace('<CONTENT_ROOT>', contentRoot);
    depotManifest = depotManifest.replace('<LOCAL_PATH>', localPath);
    depotManifest = depotManifest.replace('<DEPOT_PATH>', depotPath);
    depotManifest = depotManifest.replace('<SHOULD_ADD_CONTENT_RECURSIVELY>', "${addContentRecursively ? '1' : '0'}");
    
    def excludeString = '';
    if(excludes.length() > 0) {
        def excludeList = excludes.split(';');
        excludeString = '"FileExclusion" "' + excludeList.join('"\n  "FileExclusion" "') + '"';
    }
    depotManifest = depotManifest.replace('<EXCLUSION_STRING>', excludeString);

    file.write("${env.WORKSPACE}/depot_build_${depotID}.vdf", depotManifest);
    return "${env.WORKSPACE}/depot_build_${depotID}.vdf";
}

def tryDeploy(String steamCredentials, String appManifest) {
    def result = deploy(steamCredentials, appManifest);

    def attempts = 0;
    while((result & (SteamResult.needsTwoFactorCode | SteamResult.needsGuardCode | SteamResult.guardCodeMismatch)) != 0 && attempts < 3) {
        def guardCode = '';

        timeout(time: 2, unit: 'MINUTES') {
            guardCode = input message: 'Enter Steam Guard code', ok: 'Submit', 
                parameters: [
                    string(defaultValue: '', description: 'The Steam Guard code needed to log in to the account used by this pipeline.', name: 'Steam Guard Code', trim: true)
                ]
        }

        if(guardCode) {
            result = deploy(steamCredentials, appManifest, guardCode, (result == SteamResult.needsGuardCode) ? false : true);
        } else {
            result = SteamResult.failed;
        }
        
        ++attempts;
    }

    if(result != SteamResult.success) {
        failStage('Failed to deploy to Steam');
    }
}

private def deploy(String steamCredentials, String appManifest, String steamGuardCode = '', Boolean guardCodeIsTwoFactorCode = true) {
    def output = '';
    withCredentials([usernamePassword(credentialsId: "${steamCredentials}", passwordVariable: 'STEAM_PASS', usernameVariable: 'STEAM_USER')]) {
        try {
            if(!guardCodeIsTwoFactorCode) {
                bat label: 'Steam build', returnStdout: true, script: "\"${SteamConfig.steamcmdExe}\" set_steam_guard_code ${steamGuardCode} +quit";
            }
            
            bat label: 'Steam build', returnStdout: true, script: "\"${SteamConfig.steamcmdExe}\" +login \"${env.STEAM_USER}\" \"${env.STEAM_PASS}\" ${steamGuardCode} +run_app_build \"${appManifest}\" +quit > steamcmdoutput.txt"
            return;
        }
        catch (Exception e) {
            output = readFile 'steamcmdoutput.txt';
            log(output);
        }
        finally {
            file.delete('steamcmdoutput.txt');
        }
    }

    if(output.contains('need two-factor code')) {
        log.error('Unable to log into SteamCMD. 2FA code is required.');
        return SteamResult.needsTwoFactorCode;
    } else if(output.contains('Invalid Password')) {
        log.error('Unable to log into SteamCMD. Invalid password provided.');
        return SteamResult.invalidPassword;
    } else if(output.contains('Two-factor code mismatch')) {
        log.error('Unable to log into SteamCMD. 2FA code mismatch.');
        return SteamResult.guardCodeMismatch;
    } else if(output.contains('Assertion Failed')) {
        log.error('Assertion failed while trying to deploy to Steam.')
        return SteamResult.assertionFailed;
    } else if(output.contains('Rate Limit Exceeded')) {
        log.error('Unable to log into SteamCMD. Rate limit exceeded.')
        return SteamResult.rateLimitExceeded;
    } else if(output.contains('Account Logon Denied')) {
        log.error('Unable to log into SteamCMD. Steam Guard code is required.')
        return SteamResult.needsGuardCode;
    } else if(output.contains('ERROR!')) {
        if(output.contains('Build for depot')) {
            if(output.contains('failed : Timeout')) {
                log.error("Timed out while building for depot.");
            } else {
                log.error("Error while building for depot.");
            }
        } else if(output.contains('Content root folder does not exist')) {
            log.error("Content root folder does not exist.");
        }
        
        return SteamResult.failed;
    }

    return SteamResult.success;
}

class SteamResult {
    static final int success = 0;
    static final int failed = 1 << 1;
    static final int invalidPassword = 1 << 2;
    static final int needsTwoFactorCode = 1 << 3;
    static final int guardCodeMismatch = 1 << 4;
    static final int assertionFailed = 1 << 5;
    static final int rateLimitExceeded = 1 << 6;
    static final int needsGuardCode = 1 << 7;
}
