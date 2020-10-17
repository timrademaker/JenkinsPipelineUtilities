class VisualStudioConfig {
    static String visualStudioBaseDirectory = 'C:/Program Files (x86)/Microsoft Visual Studio/2019/Community';
    static String msBuildPath = "${visualStudioBaseDirectory}/MSBuild/Current/Bin/MSBuild.exe";
    static String vsTestPath = "${visualStudioBaseDirectory}/Common7/IDE/CommonExtensions/Microsoft/TestWindow/vstest.console.exe";
}

def init(String visualStudioBaseDirectory, String msBuildPath = '', String vsTestPath = '') {
    assert(file.dirExists(visualStudioBaseDirectory));

    VisualStudioConfig.visualStudioBaseDirectory = visualStudioBaseDirectory;

    if(msBuildPath && file.exists(msBuildPath)) {
        VisualStudioConfig.msBuildPath = msBuildPath;
    } else {
        VisualStudioConfig.msBuildPath = "${visualStudioBaseDirectory}/MSBuild/Current/Bin/MSBuild.exe";
    }

    if(vsTestPath && file.exists(vsTestPath)) {
        VisualStudioConfig.vsTestPath = vsTestPath;
    } else {
        VisualStudioConfig.vsTestPath = "${visualStudioBaseDirectory}/Common7/IDE/CommonExtensions/Microsoft/TestWindow/vstest.console.exe";
    }
}

def build(String projectPath, String platform = '', String configuration = '') {
    assert(file.nameExists(projectPath));
    assert(file.exists(VisualStudioConfig.msBuildPath));
    
    bat(label: "Build Visual Studio Solution", script: "CALL \"${VisualStudioConfig.msBuildPath}\" \"${projectPath}\" /t:build ${platform ? '/p:Platform=\"' + platform + '\"' : ''} ${configuration ? '/p:Configuration=\"'+ configuration + '\"': ''}");
}

def vsTest(String testFile, String platform = '', String logger = 'trx') {
    vsTest([testFile], platform, logger);
}

def vsTest(List<String> testFiles, String platform = '', String logger = 'trx') {
    for(f in testFiles) {
        assert(file.exists(f));
    }
    
    def result = bat(label: "Run Visual Studio Test", returnStatus: true, script: "CALL \"${VisualStudioConfig.vsTestPath}\" \"${testFiles.join('\" \"')}\" ${platform ? '--Platform:\"' + platform + '\"' : ''} --Logger:\"${logger}\"");
    
    if(result != 0) {
        unstable 'Some tests did not pass!'
    }
}