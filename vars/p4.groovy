def pull(credentialsID, workspaceTemplate, workspaceNameFormat, deleteGeneratedFiles = false, forceClean = false, quiet = true) {
    if(forceClean) {
        p4sync charset: 'none', credential: "${credentialsID}", format: "${workspaceNameFormat}", populate: forceClean(have: false, parallel: [enable: false, minbytes: '1024', minfiles: '1', threads: '4'], pin: '', quiet: quiet), source: templateSource("${workspaceTemplate}")
    } else {
        p4sync charset: 'none', credential: "${credentialsID}", format: "${workspaceNameFormat}", populate: autoClean(delete: deleteGeneratedFiles, modtime: false, parallel: [enable: false, minbytes: '1024', minfiles: '1', threads: '4'], pin: '', quiet: quiet, replace: true, tidy: false), source: templateSource("${workspaceTemplate}")
    }
}
