class UnrealConfiguration implements Serializable {
    static int engineMajorVersion = 5;
    static String engineRootDirectory = ''; // e.g. C:/Program Files/Epic Games/UE_5.1
    static String targetPlatform = 'Win64';
    static String buildConfiguration = 'Shipping';

    static ToolPaths paths;
}

class SupportedEngineVersion {
    static final int minimumMajorVersion = 4;
    static final int maximumMajorVersion = 5;
}

// Paths relative to the engine root directory
class ToolPaths {
    String editorCmdExe = '';
    String unrealBuildToolExe = '';
    String buildBat = '';
    String assetToolBat = '';
}

def init(String ueDir, String engineMajorVersion = '5', String platform = '', String configuration = '') {
    ensureUnrealDirectoryExists(ueDir, true);

    UnrealConfiguration.engineRootDirectory = ueDir;

    UnrealConfiguration.engineMajorVersion = engineMajorVersion.toInteger();
    if(UnrealConfiguration.engineMajorVersion < SupportedEngineVersion.minimumMajorVersion || UnrealConfiguration.engineMajorVersion > SupportedEngineVersion.maximumMajorVersion) {
        failStage("Unsupported engine major version '${engineMajorVersion}' specified. Supported engine major versions are versions between ${SupportedEngineVersion.minimumMajorVersion} and ${SupportedEngineVersion.maximumMajorVersion} (inclusive).");
    }

    UnrealConfiguration.paths = getPerVersionPaths(UnrealConfiguration.engineMajorVersion);

    if(platform) {
        UnrealConfiguration.targetPlatform = platform;
    }

    if(configuration) {
        UnrealConfiguration.buildConfiguration = configuration;
    }
}

def build(String projectDir, String projectName, String logFile = '', String platform = '', String configuration = '') {
    ensureUnrealDirectoryExists(UnrealConfiguration.engineRootDirectory);
    
    ensureProjectExists(projectDir, projectName);

    if(!file.dirExists("${projectDir}/Source")) {
        return;
    }

    if(!platform) {
        platform = UnrealConfiguration.targetPlatform;
    }

    if(!configuration) {
        configuration = UnrealConfiguration.buildConfiguration;
    }

    if(!logFile) {
        logFile = "${env.WORKSPACE}/logs/UnrealBuildLog-${env.BUILD_NUMBER}.log";
    }
    
    bat label: 'Generate Project Files', script: "CALL \"${UnrealConfiguration.engineRootDirectory}/${UnrealConfiguration.paths.unrealBuildToolExe}\" -projectfiles -project=\"${projectDir}/${projectName}.uproject\" -Game -Rocket -Progress -NoIntellisense -WaitMutex -Platforms=\"${platform}\" -Log=\"${logFile}\" PrecompileForTargets = PrecompileTargetsType.Any;"
    bat label: 'Build Editor Binaries', script: "CALL \"${UnrealConfiguration.engineRootDirectory}/${UnrealConfiguration.paths.buildBat}\" \"${projectName}Editor\" \"${projectDir}/${projectName}.uproject\" ${platform} Development -Log=\"${logFile}\""

    if(configuration.toLowerCase() != 'development') {
        bat label: 'Build Project Binaries', script: "CALL \"${UnrealConfiguration.engineRootDirectory}/${UnrealConfiguration.paths.buildBat}\" \"${projectName}\" \"${projectDir}/${projectName}.uproject\" ${platform} ${configuration} -Log=\"${logFile}\""
    }
}

def packageProject(String projectDir, String projectName, String outputDirectory, String logFile = '', String platform = '', String configuration = '') {
    ensureUnrealDirectoryExists(UnrealConfiguration.engineRootDirectory);
    
    ensureProjectExists(projectDir, projectName);
    if(outputDirectory == '') {
        failStage('No valid output directory set for the project to be output to!');
    }
    
    if(!platform) {
        platform = UnrealConfiguration.targetPlatform;
    }

    if(!configuration) {
        configuration = UnrealConfiguration.buildConfiguration;
    }

    if(!logFile) {
        logFile = "${env.WORKSPACE}/logs/UnrealPackageLog-${env.BUILD_NUMBER}.log";
    }

    bat label: 'Package Project', script: "CALL \"${UnrealConfiguration.engineRootDirectory}/${UnrealConfiguration.paths.assetToolBat}\" BuildCookRun -Project=\"${projectDir}/${projectName}.uproject\" -NoP4 -Distribution -Cook -Build -Stage -Pak -Rocket -Prereqs -Package -TargetPlatform=${platform} -Platform=${platform} -ClientConfig=${configuration} -ServerConfig=${configuration} -Archive -ArchiveDirectory=\"${outputDirectory}\" -Log=\"${logFile}\""
}

/*  Data validation */

def validateData(String projectDir, String projectName, Boolean turnUnstableOnFailure = false) {
    ensureUnrealDirectoryExists(UnrealConfiguration.engineRootDirectory);

    ensureProjectExists(projectDir.startsWith(env.WORKSPACE) ? projectDir : env.WORKSPACE + '/' + projectDir, projectName);
    def project = "${projectDir.startsWith(env.WORKSPACE) ? projectDir : env.WORKSPACE + '/' + projectDir}/${projectName}.uproject";
    
    def result = bat label: 'Validate Data', returnStatus: true, script: "CALL \"${UnrealConfiguration.engineRootDirectory}/${UnrealConfiguration.paths.editorCmdExe}\" \"${project}\" -stdout -fullstdlogoutput -buildmachine -nullrhi -unattended -run=DataValidation"

    if(result != 0 && turnUnstableOnFailure) {
        unstable 'Data validation returned with one or more warnings!'
    }
}

/*  Automation Testing  */

def runTestsNamed(String projectDir, String projectName, List<String> testNames, String minimumPriority = '') {
    def testsToRun = testNames.join('+');
    
    runUnrealAutomationTests(projectDir, projectName, ["RunTests Now ${testsToRun}"], minimumPriority);
}

def runTestsCheckpointed(String projectDir, String projectName, List<String> testNames, String minimumPriority = '') {
    def testsToRun = testNames.join('+');

    runUnrealAutomationTests(projectDir, projectName, ["RunCheckpointedTests Now ${testsToRun}"], minimumPriority);
}

def runTestsFiltered(String projectDir, String projectName, String filter, String minimumPriority = '') {
    if(filterIsValid(filter)) {
        runUnrealAutomationTests(projectDir, projectName, ["RunFilter Now ${filter}"], minimumPriority);
    }
}

def runAllTests(String projectDir, String projectName, String minimumPriority = '') {
    runUnrealAutomationTests(projectDir, projectName, ["RunAll Now"], minimumPriority);
}

private def runUnrealAutomationTests(String projectDir, String projectName, List<String> automationCommands, String minimumPriority) {
    ensureUnrealDirectoryExists(UnrealConfiguration.engineRootDirectory);
    
    ensureProjectExists(projectDir, projectName);
    def project = "${projectDir}/${projectName}.uproject";

    if(minimumPriority) {
        automationCommands = [minimumTestPriorityCommand(minimumPriority)] + automationCommands;
    }

    // Ensure the game has been built before trying to run tests
    build(projectDir, projectName, '', '', 'Development');

    def result = bat label: 'Run Automation Tests', returnStatus: true, script: "CALL \"${UnrealConfiguration.engineRootDirectory}/${UnrealConfiguration.paths.editorCmdExe}\" \"${projectDir}/${projectName}.uproject\" -stdout -fullstdlogoutput -buildmachine -nullrhi -unattended -NoPause -NoSplash -NoSound -ExecCmds=\"Automation ${automationCommands.join(';')};Quit\""
    
    if(result != 0) {
        unstable 'Some tests did not pass!'
    }
}

private def priorityLevelIsValid(String priorityLevel) {
    def possiblePriorityLevels = ['None', 'Low', 'Medium', 'High', 'Critical'];
    priorityLevel = priorityLevel.toLowerCase().capitalize();

    if(priorityLevel in possiblePriorityLevels) {
        return true;
    } else {
        log.error("Invalid test priority level '${priorityLevel}' specified. Valid levels: ${possiblePriorityLevels.join(', ')}.")
        return false;
    }
}

private def filterIsValid(String filter) {
    def possibleFilters = ['Engine', 'Smoke', 'Stress', 'Perf', 'Product'];
    filter = filter.toLowerCase().capitalize();

    if(filter in possibleFilters) {
        return true;
    } else {
        log.error("Invalid test filter '${filter}' specified. Valid filters: ${possibleFilters.join(', ')}.")
        return false;
    }
}

private def minimumTestPriorityCommand(String priorityLevel) {
    if(priorityLevelIsValid(priorityLevel)) {
        return "SetMinimumPriority ${priorityLevel}";
    } else {
        return '';
    }
}

private def ensureUnrealDirectoryExists(String unrealDirectory, Boolean calledFromInit = false) {
    if(!file.dirExists(unrealDirectory)) {
        failStage("Unreal Engine directory not found at specified path! (${unrealDirectory})${calledFromInit ? '' : '\nDid you set it using unreal.init?'}");
    }
}

private def ensureProjectExists(String projectDirectory, String projectName) {
    if(!file.dirExists(projectDirectory)) {
        failStage("Project directory not found at specified path! (${projectDirectory})");
    }

    if(!file.exists("${projectDirectory}/${projectName}.uproject")) {
        failStage("UProject file not found! (${projectDirectory}/${projectName}.uproject)");
    }
}

private def getPerVersionPaths(int engineMajorVersion) {
    ToolPaths paths = new ToolPaths();

    if(engineMajorVersion == 4) {
        paths.editorCmdExe = 'Engine/Binaries/Win64/UE4Editor-Cmd.exe';
        paths.unrealBuildToolExe = 'Engine/Binaries/DotNET/UnrealBuildTool.exe';
        paths.buildBat = 'Engine/Build/BatchFiles/Build.bat';
        paths.assetToolBat = 'Engine/Build/BatchFiles/RunUAT.bat';
        return paths;
    }
    else if(engineMajorVersion == 5) {
        paths.editorCmdExe = 'Engine/Binaries/Win64/UnrealEditor-Cmd.exe';
        paths.unrealBuildToolExe = 'Engine/Binaries/DotNET/UnrealBuildTool/UnrealBuildTool.exe';
        paths.buildBat = 'Engine/Build/BatchFiles/Build.bat';
        paths.assetToolBat = 'Engine/Build/BatchFiles/RunUAT.bat';
        return paths;
    }
    
    failStage("Failed to get engine paths for this version of Unreal Engine");
}
