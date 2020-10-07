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
    assert(file.dirExists(projectDir));
    assert(file.exists("${projectDir}/${projectName}.uproject"));

    if(!platform) {
        platform = UnrealConfiguration.targetPlatform;
    }

    if(!configuration) {
        configuration = UnrealConfiguration.configuration;
    }

    if(!logFile) {
        logFile = 'build.log';
    }
    
    bat label: 'Generate Project Files', script: "CALL \"${UnrealConfiguration.engineRootDirectory}\\Engine\\Binaries\\DotNET\\UnrealBuildTool.exe\" -projectfiles -project=\"${projectDir}\\${projectName}.uproject\" -game -rocket -progress -nointellisense -WaitMutex -Platforms=\"${platform}\" -log=\"${logFile}\" PrecompileForTargets = PrecompileTargetsType.Any;"

    if(configuration == 'Development') {
        bat label: 'Build binaries', script: "CALL \"${UnrealConfiguration.engineRootDirectory}\\Engine\\Build\\BatchFiles\\Build.bat\" \"${projectName}Editor\" \"${projectDir}\\${projectName}.uproject\" ${platform} ${configuration} -log=\"${logFile}\""
    }

}

def packageBuild(String projectDir, String projectName, String outputDirectory, String logFile = '', String platform = '', String configuration = '') {
    assert(file.dirExists(projectDir));
    assert(file.exists("${projectDir}/${projectName}.uproject"));
    assert(outputDirectory);
    
    if(!platform) {
        platform = UnrealConfiguration.targetPlatform;
    }

    if(!configuration) {
        configuration = UnrealConfiguration.configuration;
    }

    if(!logFile) {
        logFile = 'package.log';
    }

    bat label: 'Package project', script: "CALL \"${UnrealConfiguration.engineRootDirectory}\\Engine\\Build\\BatchFiles\\RunUAT.bat\" BuildCookRun -project=\"${projectDir}\\${projectName}.uproject\" -noP4 -Distribution -Cook -Build -Stage -Pak -Rocket -Prereqs -Package -TargetPlatform=${platform} -Platform=${platform} -ClientConfig=${configuration} -ServerConfig=${configuration} -Archive -archivedirectory=\"${outputDirectory}\" -log=\"${logFile}\""
}
