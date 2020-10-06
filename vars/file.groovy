import java.nio.file.Files
import java.nio.file.Paths

def exists(String filePath) {
    return Files.exists(Paths.get(filePath));
}

def dirExists(String directory) {
    return Files.isDirectory(Paths.get(directory));
}

def createDir(String directory) {
    File.createDirectories(Paths.get(directory));
}
