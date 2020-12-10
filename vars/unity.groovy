class UnityConfiguration implements Serializable {
    static String engineRootDirectory = '';
}


def init(String unityDir) {
    assert(file.dirExists(unityDir));

    UnityConfiguration.engineRootDirectory = unityDir;
}

def execute(String projectDir, String methodToExecute, String buildTarget = '', String logFile = '', Boolean noGraphics = true, String additionalParameters = '') {
    assert(file.dirExists(UnityConfiguration.engineRootDirectory));
    assert(file.dirExists(projectDir) && projectDir != '');
    projectDir = projectDir.replace('\\', '/');

    if(!logFile) {
        logFile = "${env.WORKSPACE}/logs/UnityLog-${env.BUILD_NUMBER}.txt";
    }

    def buildTargetStr = '';
    if(buildTarget && buildTargetIsValid(buildTarget)) {
        buildTargetStr = "-buildTarget ${buildTarget}";
    }

    def exitCode = bat label: 'Execute Unity Method', returnStatus: true, script: "CALL \"${UnityConfiguration.engineRootDirectory}/Editor/Unity.exe\" -batchmode -project \"${projectDir}\" ${noGraphics ? '-nographics' : ''} -executeMethod ${methodToExecute} ${additionalParameters} -logFile \"${logFile}\" -quit"

    if(exitCode != 0) {
        log.error("Unity method exited with a non-zero exit code!");
        failStage();
    }
}

def runTests(String projectDir, String testPlatform = '', List<String> testFilters = [], List<String> testCategories = [], String testSettingsFile = '', String testResultFile = '', Boolean noGraphics = true) {
    assert(file.dirExists(UnityConfiguration.engineRootDirectory));
    assert(file.dirExists(projectDir) && projectDir != '');
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
