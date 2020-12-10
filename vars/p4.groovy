class P4Config implements Serializable {
    static String credentials = '';
    static String workspaceNameFormat = 'jenkins-${JOB_BASE_NAME}';
    static String workspaceTemplate = '';
    static String viewMapping = '';
    static String host = '';
}

def init(String credentials, String workspaceNameFormat, String workspaceTemplate = '', String viewMapping = '', String host = '') {
    P4Config.credentials = credentials;
    P4Config.workspaceNameFormat = workspaceNameFormat;
    P4Config.workspaceTemplate = workspaceTemplate;
    P4Config.viewMapping = viewMapping;
    P4Config.host = host;
}

def pull(deleteGeneratedFiles = false, quiet = true, forceClean = false) {
    if(forceClean) {
        p4sync charset: 'none', credential: P4Config.credentials, format: P4Config.workspaceNameFormat, populate: forceClean(have: false, parallel: [enable: true, minbytes: '1024', minfiles: '1', threads: '4'], pin: '', quiet: quiet), source: templateSource(P4Config.workspaceTemplate)
    } else {
        p4sync charset: 'none', credential: P4Config.credentials, format: P4Config.workspaceNameFormat, populate: autoClean(delete: deleteGeneratedFiles, modtime: false, parallel: [enable: true, minbytes: '1024', minfiles: '1', threads: '4'], pin: '', quiet: quiet, replace: true, tidy: false), source: templateSource(P4Config.workspaceTemplate)
    }
}

def clean() {
    def p4s = p4(credential: P4Config.credentials, workspace: manualSpec(charset: 'none', cleanup: false, name: P4Config.workspaceNameFormat, pinHost: false, spec: clientSpec(allwrite: true, backup: true, changeView: '', clobber: true, compress: false, line: 'LOCAL', locked: false, modtime: false, rmdir: false, serverID: '', streamName: '', type: 'WRITABLE', view: P4Config.viewMapping)));
    p4s.run('revert', '-c', 'default', '//...');
}

def createTicket() {
   def ticket = '';

   withCredentials([usernamePassword(credentialsId: P4Config.credentials, passwordVariable: 'P4_PASS', usernameVariable: 'P4_USER')]) 
   {
      bat(label: "Trust P4 Connection", script: "echo ${P4_PASS}| p4 -p ${P4Config.host} -u ${P4_USER} trust -y");
      def result = bat(label: "Create P4 Ticket", returnStdout: true, script: "echo ${P4_PASS}| p4 -p ${P4Config.host} -u ${P4_USER} login -ap");
      ticket = result.tokenize().last();
   }
   
   return ticket
}

def unshelve(String shelveID)
{
   p4unshelve credential: P4Config.credentials, ignoreEmpty: false, resolve: 'none', shelf: shelveID, tidy: false, workspace: manualSpec(charset: 'none', cleanup: false, name: P4Config.workspaceNameFormat, pinHost: false, spec: clientSpec(allwrite: true, backup: true, changeView: '', clobber: true, compress: false, line: 'LOCAL', locked: false, modtime: false, rmdir: false, serverID: '', streamName: '', type: 'WRITABLE', view: P4Config.viewMapping))
} 

def getChangelistDescription(String changelistID) {
    def p4s = p4(credential: P4Config.credentials, workspace: manualSpec(charset: 'none', cleanup: false, name: P4Config.workspaceNameFormat, pinHost: false, spec: clientSpec(allwrite: true, backup: true, changeView: '', clobber: true, compress: false, line: 'LOCAL', locked: false, modtime: false, rmdir: false, serverID: '', streamName: '', type: 'WRITABLE', view: P4Config.viewMapping)));
    def changelist = p4s.run('describe', '-s', '-S', "${changelistID}");
    def desc = '';

    for (item in changelist) {
      for (key in item.keySet()) {
         if (key == 'desc') {
            desc = item.get(key)
         }
      }
   }

   return desc;
}

def getCurrChangelistDescription() {
   return getChangelistDescription(env.P4_CHANGELIST);
}
