class VisualStudioConfig {
    static String visualStudioBaseDirectory = 'C:/Program Files (x86)/Microsoft Visual Studio/2019/Community';
    static String msBuildPath = "${visualStudioBaseDirectory}/MSBuild/Current/Bin/MSBuild.exe";
}

def init(String visualStudioBaseDirectory, String msBuildPath = '') {
    assert(file.dirExists(visualStudioBaseDirectory));

    VisualStudioConfig.visualStudioBaseDirectory = visualStudioBaseDirectory;

    if(msBuildPath && file.exists(msBuildPath)) {
        VisualStudioConfig.msBuildPath = msBuildPath;
    } else {
        VisualStudioConfig.msBuildPath = "${visualStudioBaseDirectory}/MSBuild/Current/Bin/MSBuild.exe";
    }
}

def build(String projectPath, String platform, String configuration) {
    assert(file.nameExists(projectPath));
    assert(file.exists(VisualStudioConfig.msBuildPath));
    
    bat(label: "Build Visual Studio Solution", script: "CALL \"${VisualStudioConfig.msBuildPath}\" \"${projectPath}\" /t:build /p:Platform=\"${platform}\" /p:Configuration=\"${configuration}\"");
}
