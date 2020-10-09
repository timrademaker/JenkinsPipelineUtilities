class UnityConfiguration {
    static String engineRootDirectory = '';
}


def init(String unityDir) {
    assert(file.dirExists(unityDir));

    UnityConfiguration.engineRootDirectory = unityDir;
}

def execute(String projectDir, String executeMethod, String logFile = '', Boolean noGraphics = false, String additionalParameters = '') {
    assert(file.dirExists(UnityConfiguration.engineRootDirectory));
    assert(file.dirExists(projectDir));

    if(!logFile) {
        logFile = "${env.WORKSPACE}/logs/UnityLog-${env.BUILD_NUMBER}.txt";
    }

    def exitCode = bat label: 'Execute Unity Method', returnStatus: true, script: "CALL \"${UnityConfiguration.engineRootDirectory}/Editor/Unity.exe\" -batchmode ${noGraphics ? '-nographics' : ''} -executeMethod ${executeMethod} ${additionalParameters} -logFile \"${logFile}\" -quit"

    if(exitCode != 0) {
        log.error("Unity method exited with a non-zero error code!");
        failStage();
    }
}

def runTests(String projectDir, String testPlatform = '', List<String> testFilters = [], List<String> testCategories = [], String testSettingsFile = '', String testResultFile = '', Boolean noGraphics = false) {
    assert(file.dirExists(UnityConfiguration.engineRootDirectory));
    assert(file.dirExists(projectDir));

    def argumentString = "-batchmode ${noGraphics ? '-nographics' : ''} -runTest";

    if(testPlatformIsValid(testPlatform)) {
        argumentString += " -testPlatform ${testPlatform}"
    }

    if(testFilter.size() > 0) {
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

private def testPlatformIsValid(String platform) {
    if(!platform) {
        return false;
    }

    def possiblePlatforms = ['EditMode', 'PlayMode', 'StandaloneWindows', 'StandaloneWindows64', 'StandaloneLinux64', 'StandaloneOSX', 'iOS', 'Android', 'PS4', 'XboxOne'];

    if(possiblePlatforms*.toLowerCase().contains(platform.toLowerCase())) {
        return true;
    } else {
        log.error("Invalid test platform '${platform}' specified. Valid platforms: ${possiblePlatforms.join(', ')}.")
        return false;
    }
}
