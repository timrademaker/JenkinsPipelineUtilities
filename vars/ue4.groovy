class UnrealConfiguration {
    static String engineRootDirectory = '';
    static String targetPlatform = 'Win64';
    static String buildConfiguration = 'Development';
}


def init(String ueDir, String platform = '', String configuration = '') {
    assert(file.dirExists(ueDir));

    UnrealConfiguration.engineRootDirectory = ueDir;

    if(platform) {
        UnrealConfiguration.targetPlatform = platform;
    }

    if(configuration) {
        UnrealConfiguration.buildConfiguration = configuration;
    }
}

def build(String projectDir, String projectName, String logFile = '', String platform = '', String configuration = '') {
    assert(file.exists("${projectDir}/${projectName}.uproject"));

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
    
    bat label: 'Generate Project Files', script: "CALL \"${UnrealConfiguration.engineRootDirectory}/Engine/Binaries/DotNET/UnrealBuildTool.exe\" -projectfiles -project=\"${projectDir}/${projectName}.uproject\" -Game -Rocket -Progress -NoIntellisense -WaitMutex -Platforms=\"${platform}\" -Log=\"${logFile}\" PrecompileForTargets = PrecompileTargetsType.Any;"

    if(configuration == 'Development') {
        bat label: 'Build Binaries', script: "CALL \"${UnrealConfiguration.engineRootDirectory}/Engine/Build/BatchFiles/Build.bat\" \"${projectName}Editor\" \"${projectDir}/${projectName}.uproject\" ${platform} ${configuration} -Log=\"${logFile}\""
    }
}

def packageBuild(String projectDir, String projectName, String outputDirectory, String logFile = '', String platform = '', String configuration = '') {
    assert(file.exists("${projectDir}/${projectName}.uproject"));
    assert(outputDirectory);
    
    if(!platform) {
        platform = UnrealConfiguration.targetPlatform;
    }

    if(!configuration) {
        configuration = UnrealConfiguration.buildConfiguration;
    }

    if(!logFile) {
        logFile = "${env.WORKSPACE}/logs/UnrealPackageLog-${env.BUILD_NUMBER}.log";
    }

    bat label: 'Package Project', script: "CALL \"${UnrealConfiguration.engineRootDirectory}/Engine/Build/BatchFiles/RunUAT.bat\" BuildCookRun -Project=\"${projectDir}/${projectName}.uproject\" -NoP4 -Distribution -Cook -Build -Stage -Pak -Rocket -Prereqs -Package -TargetPlatform=${platform} -Platform=${platform} -ClientConfig=${configuration} -ServerConfig=${configuration} -Archive -ArchiveDirectory=\"${outputDirectory}\" -Log=\"${logFile}\""
}

/*  Automation Testing  */

def runTestsNamed(String projectDir, String projectName, List<String> testNames, String minimumPriority = '') {
    def testsToRun = testNames.join('+');
    
    runUnrealAutomationTests(projectDir, projectName, ["RunTests Now ${testsToRun}"], minimumPriority);
}

def runTestsContaining(String projectDir, String projectName, String content, String minimumPriority = '') {
    runUnrealAutomationTests(projectDir, projectName, ["RunCheckpointedTests Now ${content}"], minimumPriority);
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
    def project = "${projectDir}/${projectName}.uproject";
    assert(file.exists(project));

    if(minimumPriority) {
        automationCommands = [minimumTestPriorityCommand(minimumPriority)] + automationCommands;
    }

    def command = '';
    for(cmd in automationCommands) {
        if(cmd) {
            command += "Automation ${cmd};"
        }
    }
    // Remove trailing semicolon
    command = "${command.substring(0, command.length() - 1)}";

    // Ensure the game has been built before trying to run tests
    build(projectDir, projectName, configuration: 'Development');

    def result = bat label: 'Run Automation Tests', returnStatus: true, script: "CALL \"${UnrealConfiguration.engineRootDirectory}/Engine/Binaries/Win64/UE4Editor-cmd.exe\" \"${projectDir}/${projectName}.uproject\" -stdout -fullstdlogoutput -buildmachine -nullrhi -unattended -NoPause -NoSplash -NoSound -ExecCmds=\"${command};Quit\""
    
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
