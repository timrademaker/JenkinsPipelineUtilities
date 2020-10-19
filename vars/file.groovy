def exists(String filePath) {
    def ret = powershell(label: 'Check if file exists', returnStdout: true, script: "Test-Path '${filePath}' -PathType Leaf");
    return ret.replaceAll("\\s", "").equals("True");
}

def dirExists(String directory) {
    def ret = powershell(label: 'Check if folder exists', returnStdout: true, script: "Test-Path '${directory}' -PathType Container");
    return ret.replaceAll("\\s", "").equals("True");
}

def nameExists(String path) {
    def ret = powershell(label: 'Check if file exists', returnStdout: true, script: "Test-Path '${path}' -PathType Any");
    return ret.replaceAll("\\s", "").equals("True");
}

def create(String filePath, String content) {
    if(!nameExists(filePath)) {
        writeFile(file: filePath, text: content);
    }
}

def write(String filePath, String content) {
    writeFile(file: filePath, text: content);
}

def createDir(String directory) {
    if(!nameExists(directory)) {
        powershell(label: 'Create folder', returnStdout: false, script: "New-Item '${directory}' -ItemType 'directory'");
    }
}

def delete(String path) {
    if(nameExists(path)) {
        powershell(label: 'Delete item', returnStdout: false, script: "Remove-Item '${path}' -Recurse -Confirm:\$false");
    }
}

def zip(String pathToCompress, String destinationPath) {
    assert(nameExists(pathToCompress));

    powershell(label: "Zip '${pathToCompress}''", script: "Compress-Archive -Path '${pathToCompress}' -DestinationPath '${destinationPath}'");
}

def unzip(String zipFile, String destinationPath) {
    assert(exists(zipFile));

    powershell(label: "Unzip '${zipFile}''", script: "Expand-Archive -Path '${zipFile}' -DestinationPath '${destinationPath}'");
}

def download(String downloadUrl, String outputFile) {
    powershell(label: 'Download File', script: "Invoke-Webrequest -URI ${downloadUrl} -OutFile \"${outputFile}\"");
}
