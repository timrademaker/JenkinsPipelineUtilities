def exists(String filePath) {
    if(filePath == '') {
        return false;
    }

    def ret = powershell(label: 'Check if file exists', returnStdout: true, script: "Test-Path '${filePath}' -PathType Leaf");
    return ret.replaceAll("\\s", "").equals("True");
}

def dirExists(String directory) {
    if(directory == '') {
        return false;
    }

    def ret = powershell(label: 'Check if folder exists', returnStdout: true, script: "Test-Path '${directory}' -PathType Container");
    return ret.replaceAll("\\s", "").equals("True");
}

def nameExists(String path) {
    if(path == '') {
        return false;
    }

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

def copy(String fromPath, String toPath, Boolean force = false, Boolean recurse = true) {
    if(nameExists(fromPath)) {
        powershell(label: 'Copy item', returnStdout: false, script: "Copy-Item -Path '${fromPath}' -Destination '${toPath}' ${recurse ? '-Recurse' : ''} ${force ? '-Force' : ''}");
    }
    else {
        log.error("Tried to copy '${fromPath}', but this path couldn't be found!");
    }
}

def rename(String path, String newName, Boolean force = false) {
    if(nameExists(path)) {
        powershell(label: 'Rename item', returnStdout: false, script: "Rename-Item -Path '${path}' -NewName '${newName}' ${force ? '-Force' : ''}");
    } else {
        log.error("Tried to rename '${path}', but this couldn't be found!");
    }
}

def zip(String pathToCompress, String destinationPath, Boolean optimalCompression = true) {
    if(!nameExists(pathToCompress)) {
        log.error("Unable to zip ${pathToCompress} as the path can't be found");
        return;
    }

    compressionLevel = optimalCompression ? '[System.IO.Compression.CompressionLevel]::Optimal' : '[System.IO.Compression.CompressionLevel]::Fastest';

    powershell(label: "Zip '${pathToCompress}'", script: """Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::CreateFromDirectory('${pathToCompress}', '${destinationPath}', ${compressionLevel}, \$false)""");
}

def unzip(String zipFile, String destinationPath) {
    if(!exists(zipFile)) {
        log.error("Unable to unzip ${zipFile} as the archive can't be found");
        return;
    }

    powershell(label: "Unzip '${zipFile}'", script: """Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::ExtractToDirectory('${zipFile}', '${destinationPath}')""");
}

def download(String downloadUrl, String outputFile) {
    powershell(label: 'Download File', script: "Invoke-Webrequest -URI ${downloadUrl} -OutFile \"${outputFile}\"");
}
