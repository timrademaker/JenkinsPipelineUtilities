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


def init(String ueDir, String projectDir, String projectName, String outputDirectory, String platform = config.targetPlatform, String configuration = config.buildConfiguration, String logFile = config.logFile) {
    // TODO: Ensure dirs are valid

    config.ueRootDirectory = ueDir;
    config.projectDirectory = projectDir;
    config.targetPlatform = platform;
    config.buildConfiguration = configuration;
    
    config.logFile = logFile;
    config.buildOutDirectory = outputDirectory;
}

def build(String ueDir = config.engineRootDirectory, String projectDir = config.projectDirectory, String projectName = config.projectName, String outputDirectory = config.buildOutDirectory, String platform = config.targetPlatform, String configuration = config.buildConfiguration, String logFile = config.logFile) {
    assert(ueDir);
    assert(projectDir);
    assert(projectName);
    assert(outputDirectory);
    assert(platform);
    assert(configuration);
    
    bat label: 'Generate Project Files', script: "CALL \"${ueDir}\\Engine\\Binaries\\DotNET\\UnrealBuildTool.exe\" -projectfiles -project=\"${projectDir}\\${projectName}.uproject\" -game -rocket -progress -2019 -nointellisense -Platforms=\"${platform}\" -log=\"${logFile}\" PrecompileForTargets = PrecompileTargetsType.Any;"

    if(config.buildConfiguration == 'Development') {
        bat label: 'Build binaries', script: "CALL \"${ueDir}\\Engine\\Build\\BatchFiles\\Build.bat\" \"${projectName}Editor\" \"${projectDir}\\${projectName}.uproject\" ${platform} ${configuration} -log=\"${logFile}\""
    }

}

def package() {

}