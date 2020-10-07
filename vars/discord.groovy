import groovy.json.JsonOutput

class DiscordField {
    String name;
    String value;
}

/*
class DiscordColor {
    public static int green = 3066993;
    public static int red = 15158332;
    public static int yellow = 16776960;
}
*/

def sendMessage(String webhookUrl, String title, String description, String color, String url = '', List<DiscordField> fields = []) {
    def embed = [title: title, description: description, color: color.toInteger()]

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
