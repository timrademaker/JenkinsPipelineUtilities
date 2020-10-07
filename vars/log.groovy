def info(message) {
    echo "INFO: ${message}"
}

def warn(message) {
    echo "WARNING: ${message}"
}

def error(message) {
    echo "ERROR: ${message}"
}

def parse(String logparserRulePath = '', Boolean showGraphs = true) {
    if(!logparserRulePath) {
        parseRules = libraryResource 'com/timrademaker/logparserConfig.txt'
        logparserRulePath = 'logparserRules.txt'
        file.create(logparserRulePath, parseRules);
    }

    logParser(projectRulePath: logparserRulePath, showGraphs: showGraphs, useProjectRule: true)
}
