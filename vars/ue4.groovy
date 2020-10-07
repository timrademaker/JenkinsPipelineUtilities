class ueConfiguration {
    String engineRootDirectory = '';
    String targetPlatform = 'Win64';
    String buildConfiguration = 'Development';
}

def ueConfig = new ueConfiguration();


def init(String ueDir, String platform = '', String configuration = '') {
    assert(file.dirExists(ueDir));

    ueConfig.ueRootDirectory = ueDir;

    if(platform) {
        ueConfig.targetPlatform = platform;
    }

    if(configuration) {
        ueConfig.buildConfiguration = configuration;
    }
}

def build(String projectDir, String projectName, String outputDirectory, String logFile = '', String platform = '', String configuration = '') {
    assert(file.dirExists(projectDir));
    assert(file.exists("${projectDir}/${projectName}.uproject"));
    assert(outputDirectory);

    if(!platform) {
        platform = ueConfig.targetPlatform;
    }

    if(!configuration) {
        configuration = ueConfig.configuration;
    }

    if(!logFile) {
        logFile = 'build.log';
    }
    
    bat label: 'Generate Project Files', script: "CALL \"${ueConfig.engineRootDirectory}\\Engine\\Binaries\\DotNET\\UnrealBuildTool.exe\" -projectfiles -project=\"${projectDir}\\${projectName}.uproject\" -game -rocket -progress -nointellisense -WaitMutex -Platforms=\"${platform}\" -log=\"${logFile}\" PrecompileForTargets = PrecompileTargetsType.Any;"

    if(configuration == 'Development') {
        bat label: 'Build binaries', script: "CALL \"${ueConfig.engineRootDirectory}\\Engine\\Build\\BatchFiles\\Build.bat\" \"${projectName}Editor\" \"${projectDir}\\${projectName}.uproject\" ${platform} ${configuration} -log=\"${logFile}\""
    }

}

def packageBuild(String projectDir, String projectName, String outputDirectory, String logFile = '', String platform = '', String configuration = '') {
    assert(file.dirExists(projectDir));
    assert(file.exists("${projectDir}/${projectName}.uproject"));
    assert(outputDirectory);
    
    if(!platform) {
        platform = ueConfig.targetPlatform;
    }

    if(!configuration) {
        configuration = ueConfig.configuration;
    }

    if(!logFile) {
        logFile = 'package.log';
    }

    bat label: 'Package project', script: "CALL \"${ueConfig.engineRootDirectory}\\Engine\\Build\\BatchFiles\\RunUAT.bat\" BuildCookRun -project=\"${projectDir}\\${projectName}.uproject\" -noP4 -Distribution -Cook -Build -Stage -Pak -Rocket -Prereqs -Package -TargetPlatform=${platform} -Platform=${platform} -ClientConfig=${configuration} -ServerConfig=${configuration} -Archive -archivedirectory=\"${outputDirectory}\" -log=\"${logFile}\""
}
