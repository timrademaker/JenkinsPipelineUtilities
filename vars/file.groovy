def exists(String filePath) {
    def ret = powershell(label: 'Check if file exists', returnStdout: true, script: "Test-Path '${directory}' -PathType Leaf");
    return ret.replaceAll("\\s", "").equals("True");
}

def dirExists(String directory) {
    def ret = powershell(label: 'Check if folder exists', returnStdout: true, script: "Test-Path '${directory}' -PathType Container");
    return ret.replaceAll("\\s", "").equals("True");
}

def nameExists(String path) {
    def ret = powershell(label: 'Check if file exists', returnStdout: true, script: "Test-Path '${directory}' -PathType Any");
    return ret.replaceAll("\\s", "").equals("True");
}

def create(String filePath, String content) {
    if(!nameExists(filePath)) {
        writeFile(file: filePath, text: content);
    }
}

def createDir(String directory) {
    if(!nameExists(directory)) {
        powershell(label: 'Create folder', returnStdout: false, script: "New-Item '${directory}' -ItemType 'directory'");
    }
}
