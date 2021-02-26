class VisualStudioConfig implements Serializable {
    static String visualStudioBaseDirectory = 'C:/Program Files (x86)/Microsoft Visual Studio/2019/Community';
    static String msBuildPath = "${visualStudioBaseDirectory}/MSBuild/Current/Bin/MSBuild.exe";
    static String vsTestPath = "${visualStudioBaseDirectory}/Common7/IDE/CommonExtensions/Microsoft/TestWindow/vstest.console.exe";
}

def init(String visualStudioBaseDirectory, String msBuildPath = '', String vsTestPath = '') {
    if(!file.dirExists(visualStudioBaseDirectory)) {
        failStage("Visual Studio directory not found at specified path! (${visualStudioBaseDirectory})");
    }

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
    if(!file.nameExists(projectPath)) {
        failStage("Project directory not found at specified path! (${projectPath})");
    }
    if(!file.exists(VisualStudioConfig.msBuildPath)) {
        if(VisualStudioConfig.visualStudioBaseDirectory == '') {
            failStage("Visual Studio base directory not set! Did you call visualstudio.init?");
        } else {
            failStage("MSBuild not found! (${VisualStudioConfig.msBuildPath})");
        }
    }
    
    bat(label: "Build Visual Studio Solution", script: "CALL \"${VisualStudioConfig.msBuildPath}\" \"${projectPath}\" /t:build ${platform ? '/p:Platform=\"' + platform + '\"' : ''} ${configuration ? '/p:Configuration=\"'+ configuration + '\"': ''}");
}

def vsTest(String testFile, String platform = '', List<String> testNames = [], String logger = 'trx', String additionalFlags = '') {
    vsTest([testFile], platform, testNames, logger, additionalFlags);
}

def vsTest(List<String> testFiles, String platform = '', List<String> testNames = [], String logger = 'trx', String additionalFlags = '') {
    for(f in testFiles) {
        if(!file.exists(f)) {
            failStage("Test file not found! (${f})");
        }
    }
    
    def result = bat(label: "Run Visual Studio Test", returnStatus: true, script: "CALL \"${VisualStudioConfig.vsTestPath}\" \"${testFiles.join('\" \"')}\" ${platform ? '--Platform:\"' + platform + '\"' : ''} ${testNames ? '/Tests:' + testNames.join(',') : ''} --Logger:\"${logger}\" ${additionalFlags}");
    
    if(result != 0) {
        unstable 'Some tests did not pass!'
    }
}
