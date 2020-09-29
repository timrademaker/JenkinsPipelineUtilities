def info(message) {
    echo "INFO: ${message}"
}

def warn(message) {
    echo "WARNING: ${message}"
}

def error(message) {
    echo "ERROR: ${message}"
}

def parse(logparserRulePath = "logparserRules.txt", showGraphs = true) {
    logParser(projectRulePath: logparserRulePath, showGraphs: showGraphs, useProjectRule: true)
}
