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

def zip(String pathToCompress, String destinationPath, Boolean optimalCompression = true) {
    assert(nameExists(pathToCompress));

    compressionLevel = optimalCompression ? '[System.IO.Compression.CompressionLevel]::Optimal' : '[System.IO.Compression.CompressionLevel]::Fastest';

    powershell(label: "Zip '${pathToCompress}''", script: """Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::CreateFromDirectory('${pathToCompress}', '${destinationPath}', ${compressionLevel}, $false)""");
}

def unzip(String zipFile, String destinationPath) {
    assert(exists(zipFile));

    powershell(label: "Unzip '${zipFile}''", script: """Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::ExtractToDirectory('${zipFile}', '${destinationPath}')""");
}

def download(String downloadUrl, String outputFile) {
    powershell(label: 'Download File', script: "Invoke-Webrequest -URI ${downloadUrl} -OutFile \"${outputFile}\"");
}
