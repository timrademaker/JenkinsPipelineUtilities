def call(String message) {
    echo "${message}";
}

def info(String message) {
    echo "INFO: ${message}"
}

def warn(String message) {
    echo "WARNING: ${message}"
}

def error(String message) {
    echo "ERROR: ${message}"
}

def currentStage() {
    echo "Stage: ${STAGE_NAME}"
}

def parse(String logparserRulePath = '', Boolean showGraphs = true) {
    if(!logparserRulePath) {
        parseRules = libraryResource 'com/timrademaker/logparserConfig.txt'
        logparserRulePath = 'logparserRules.txt'
        file.write(logparserRulePath, parseRules);
    }

    logParser(projectRulePath: logparserRulePath, showGraphs: showGraphs, useProjectRule: true)
}
