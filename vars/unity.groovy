class UnityConfiguration implements Serializable {
    static String engineRootDirectory = '';
}


def init(String unityDir) {
    ensureUnityDirectoryExists(unityDir, true);

    UnityConfiguration.engineRootDirectory = unityDir;
}

def execute(String projectDir, String methodToExecute, String buildTarget = '', String logFile = '', Boolean noGraphics = true, String additionalParameters = '', Boolean outputLogOnFailure = true) {
    ensureUnityDirectoryExists(UnityConfiguration.engineRootDirectory);
    ensureProjectDirectoryExists(projectDir);

    projectDir = projectDir.replace('\\', '/');

    if(!logFile) {
        logFile = '-';
    }

    def buildTargetStr = '';
    if(buildTarget && buildTargetIsValid(buildTarget)) {
        buildTargetStr = "-buildTarget ${buildTarget}";
    }

    def exitCode = bat label: 'Execute Unity Method', returnStatus: true, script: "CALL \"${UnityConfiguration.engineRootDirectory}/Editor/Unity.exe\" -batchmode -projectPath \"${projectDir}\" ${noGraphics ? '-nographics' : ''} -executeMethod ${methodToExecute} ${additionalParameters} -logFile \"${logFile}\" -quit"

    if(exitCode != 0) {
        if(outputLogOnFailure && logFile != '-') {
            logContent = readFile logFile
            log(logContent)
        }

        failStage('Unity method exited with a non-zero exit code!');
    }
}

def runTests(String projectDir, String testPlatform = '', List<String> testFilters = [], List<String> testCategories = [], String testSettingsFile = '', String testResultFile = '', Boolean noGraphics = true) {
    ensureUnityDirectoryExists(UnityConfiguration.engineRootDirectory);
    ensureProjectDirectoryExists(projectDir);

    projectDir = projectDir.replace('\\', '/');

    def argumentString = "-batchmode -projectPath \"${projectDir}\" ${noGraphics ? '-nographics' : ''} -runTests -silent-crashes";

    if(testPlatformIsValid(testPlatform)) {
        argumentString += " -testPlatform ${testPlatform}"
    }

    if(testFilters.size() > 0) {
        argumentString += ' -testFilter ';
        for(filter in testFilters) {
            argumentString += "${filter};";
        }
        // Remove trailing semicolon
        argumentString = "${argumentString.substring(0, argumentString.length() - 1)}";
    }

    def categoryString = '';
    if(testCategories.size() > 0) {
        argumentString += ' -testCategory ';
        for(category in testCategories) {
            argumentString += "${category};";
        }
        // Remove trailing semicolon
        argumentString = "${argumentString.substring(0, argumentString.length() - 1)}";
    }

    if(!testResultFile) {
        testResultFile = "${env.WORKSPACE}/logs/UnityTestLog-${env.BUILD_NUMBER}.xml";
    }

    argumentString += " -testResults \"${testResultFile}\"";

    def exitCode = bat label: 'Run Tests', returnStatus: true, script: "CALL \"${UnityConfiguration.engineRootDirectory}/Editor/Unity.exe\" ${argumentString}"
    
    if(exitCode != 0) {
        unstable 'Some tests did not pass!'
    }
}

private def buildTargetIsValid(String target) {
    if(!target) {
        return false;
    }

    def possibleTargets = ['Standalone', 'Win', 'Win64', 'OSXUniversal', 'Linux64', 'iOS', 'Android', 'WebGL', 'XboxOne', 'PS4', 'WindowsStoreApps', 'Switch', 'tvOS'];
    target = target.toLowerCase();

    for(possibleTarget in possibleTargets) {
        if(target == possibleTarget.toLowerCase()) {
            return true;
        }
    }

    log.error("Invalid build target '${target}' specified. Valid targets: ${possibleTargets.join(', ')}.")
    return false;
}

private def testPlatformIsValid(String platform) {
    if(!platform) {
        return false;
    }

    def possiblePlatforms = ['EditMode', 'PlayMode', 'StandaloneWindows', 'StandaloneWindows64', 'StandaloneLinux64', 'StandaloneOSX', 'iOS', 'Android', 'PS4', 'XboxOne'];
    platform = platform.toLowerCase();

    for(possiblePlat in possiblePlatforms) {
        if(platform == possiblePlat.toLowerCase()) {
            return true;
        }
    }

    log.error("Invalid test platform '${platform}' specified. Valid platforms: ${possiblePlatforms.join(', ')}.")
    return false;
}

private def ensureUnityDirectoryExists(String unityDirectory, Boolean calledFromInit = false) {
    if(!file.dirExists(unityDirectory)) {
        failStage("Unity directory not found at specified path! (${unityDirectory})${calledFromInit ? '' : '\nDid you set it using unity.init?'}");
    }
}

private def ensureProjectDirectoryExists(String projectDirectory) {
    if(projectDirectory == '' || !file.dirExists(projectDirectory)) {
        failStage("Project directory does not exist! (${projectDirectory})")
    }
}
