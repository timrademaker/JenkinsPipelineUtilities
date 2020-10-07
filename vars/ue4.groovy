class ueConfig {
    String engineRootDirectory = '';
    String projectDirectory = '';
    String projectName = '';
    String targetPlatform = 'Win64';
    String buildConfiguration = 'Development';

    String logFile = '';
    String buildOutDirectory = '';
}

def config = new ueConfig();


def init(String ueDir, String projectDir = config.projectDirectory, String projectName = config.projectName, String outputDirectory = config.buildOutDirectory, String platform = config.targetPlatform, String configuration = config.buildConfiguration, String logFile = config.logFile) {
    assert(file.dirExists(ueDir));

    config.ueRootDirectory = ueDir;
    config.projectDirectory = projectDir;
    config.targetPlatform = platform;
    config.buildConfiguration = configuration;
    
    config.logFile = logFile;
    config.buildOutDirectory = outputDirectory;
}

def build(String projectDir = config.projectDirectory, String projectName = config.projectName, String outputDirectory = config.buildOutDirectory, String platform = config.targetPlatform, String configuration = config.buildConfiguration, String logFile = config.logFile) {
    assert(file.dirExists(projectDir));
    assert(file.exists("${projectDir}/${projectName}.uproject"));
    assert(outputDirectory);
    assert(platform);
    assert(configuration);
    
    bat label: 'Generate Project Files', script: "CALL \"${config.engineRootDirectory}\\Engine\\Binaries\\DotNET\\UnrealBuildTool.exe\" -projectfiles -project=\"${projectDir}\\${projectName}.uproject\" -game -rocket -progress -nointellisense -WaitMutex -Platforms=\"${platform}\" -log=\"${logFile}\" PrecompileForTargets = PrecompileTargetsType.Any;"

    if(config.buildConfiguration == 'Development') {
        bat label: 'Build binaries', script: "CALL \"${config.engineRootDirectory}\\Engine\\Build\\BatchFiles\\Build.bat\" \"${projectName}Editor\" \"${projectDir}\\${projectName}.uproject\" ${platform} ${configuration} -log=\"${logFile}\""
    }

}

def package(String ueDir = config.engineRootDirectory, String projectDir = config.projectDirectory, String projectName = config.projectName, String outputDirectory = config.buildOutDirectory, String platform = config.targetPlatform, String configuration = config.buildConfiguration, String logFile = config.logFile) {
    assert(file.dirExists(projectDir));
    assert(file.exists("${projectDir}/${projectName}.uproject"));
    assert(outputDirectory);
    assert(platform);
    assert(configuration);

    bat label: 'Package project', script: "CALL \"${config.engineRootDirectory}\\Engine\\Build\\BatchFiles\\RunUAT.bat\" BuildCookRun -project=\"${projectDir}\\${projectName}.uproject\" -noP4 -Distribution -Cook -Build -Stage -Pak -Rocket -Prereqs -Package -TargetPlatform=${platform} -Platform=${platform} -ClientConfig=${configuration} -ServerConfig=${configuration} -Archive -archivedirectory=\"${outputDirectory}\" -log=\"${logFile}\""
}
