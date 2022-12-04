def init(String ueDir, String platform = '', String configuration = '') {
    log.warning("ue4 has been replaced with unreal. Please update your pipeline script, as ue4 might be removed in a future version of this library.");
    unreal.init(ueDir, '4', platform, configuration);
}

def build(String projectDir, String projectName, String logFile = '', String platform = '', String configuration = '') {
    log.warning("ue4 has been replaced with unreal. Please update your pipeline script, as ue4 might be removed in a future version of this library.");
    unreal.build(projectDir, projectName, logFile, platform, configuration);
}

def packageProject(String projectDir, String projectName, String outputDirectory, String logFile = '', String platform = '', String configuration = '') {
    log.warning("ue4 has been replaced with unreal. Please update your pipeline script, as ue4 might be removed in a future version of this library.");
    unreal.packageProject(projectDir, projectName, outputDirectory, logFile, platform, configuration);
}

/*  Data validation */

def validateData(String projectDir, String projectName, Boolean turnUnstableOnFailure = false) {
    log.warning("ue4 has been replaced with unreal. Please update your pipeline script, as ue4 might be removed in a future version of this library.");
    unreal.validateData(projectDir, projectName, turnUnstableOnFailure);
}

/*  Automation Testing  */

def runTestsNamed(String projectDir, String projectName, List<String> testNames, String minimumPriority = '') {
    log.warning("ue4 has been replaced with unreal. Please update your pipeline script, as ue4 might be removed in a future version of this library.");
    unreal.runTestsNamed(projectDir, projectName, testNames, minimumPriority);
}

def runTestsCheckpointed(String projectDir, String projectName, List<String> testNames, String minimumPriority = '') {
    log.warning("ue4 has been replaced with unreal. Please update your pipeline script, as ue4 might be removed in a future version of this library.");
    unreal.runTestsNamed(projectDir, projectName, testNames, minimumPriority);
}

def runTestsFiltered(String projectDir, String projectName, String filter, String minimumPriority = '') {
    log.warning("ue4 has been replaced with unreal. Please update your pipeline script, as ue4 might be removed in a future version of this library.");
    unreal.runTestsFiltered(projectDir, projectName, filter, minimumPriority);
}

def runAllTests(String projectDir, String projectName, String minimumPriority = '') {
    log.warning("ue4 has been replaced with unreal. Please update your pipeline script, as ue4 might be removed in a future version of this library.");
    unreal.runAllTests(projectDir, projectName, minimumPriority);
}
