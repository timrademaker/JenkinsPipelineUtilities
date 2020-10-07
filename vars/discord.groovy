import groovy.json.JsonOutput

class DiscordField {
    String name;
    String value;
}

enum DiscordColor {
    green(3066993),
    red(15158332),
    yellow(16776960)
    
    DiscordColor(int value) {
        this.value = value;
    }
    private final int value;
  
    int getValue() {
        value; 
    }
}

def sendMessage(String webhookUrl, String title, String description, DiscordColor color, String url = '', List<DiscordField> fields = []) {
    def embed = [title: title, description: description, color: color.value]

    if(url) {
        embed.url = url;
    }

    def embFields = []
    def i = 0;
    for(f in fields) {
        embFields[i] = [name: f.name, value: f.value];
        i++;
    }
    
    if(embFields) {
        embed.fields = embFields;
    }

    def msg = JsonOutput.toJson([embeds: [embed]]).replace('"','""');
    bat label: 'Discord webhook', script: "curl -X POST -H \"Content-Type: application/json\" -d \"${msg}\" ${webhookUrl}"
}
