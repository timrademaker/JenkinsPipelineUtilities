import groovy.json.JsonOutput

/*
class DiscordField {
    String name;
    String value;
}
*/

/*
class DiscordColor {
    public static int green = 3066993;
    public static int red = 15158332;
    public static int yellow = 16776960;
}
*/

def sendMessage(String webhookUrl, String message) {
    def msg = "{\"\"content\"\": \"\"${message}\"\"}"
    bat label: 'Discord webhook - Message', script: "curl -X POST -H \"Content-Type: application/json\" -d \"${msg}\" ${webhookUrl}"
}

def sendEmbed(String webhookUrl, String title, String description = '', String color = '0', List<String[]> fields = [], String footer = '', String url = '') {
    def embed = [title: title, description: description, color: color.toInteger()]

    if(footer) {
        embed.footer = [text: footer];
    }

    if(url) {
        embed.url = url;
    }

    def embFields = []
    def i = 0;
    for(f in fields) {
        embFields[i] = [name: f[0], value: f[1]];
        i++;
    }

    if(embFields) {
        embed.fields = embFields;
    }

    def msg = JsonOutput.toJson([embeds: [embed]]).replace('"','""');
    bat label: 'Discord webhook - Embed', script: "curl -X POST -H \"Content-Type: application/json\" -d \"${msg}\" ${webhookUrl}"
}

def sendFiles(String webhookUrl, List<String> files, String message = '') {
    def fileStr = '';
    for(i = 0; i < files.size(); ++i) {
        if(file.exists(files[i])) {
            fileStr += " -F \"file${i}=@${files[i]}\"";
        }
    }
    
    def msg = "{\"\"content\"\": \"\"${message}\"\"}";
    bat label: 'Discord webhook - Files', script: "curl -H \"Content-Type: multipart/form-data\" ${fileStr} -F \"payload_json=${msg}\" ${webhookUrl}"
}
