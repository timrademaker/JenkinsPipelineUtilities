import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def createGroup(String groupName, List groupMembers, String groupType) {
   return JsonOutput.toJson([
      name: groupName,
      discordID: groupDiscordID,
      swarmID: groupSwarmID,
      type: groupType
   ]);
}

def mentionGroup(groups, String groupName) {
   def groupType = ""
   def discordID = ""
   def groupsParsed = new JsonSlurper().parseText(groups)

   groupsParsed.groups.each { group ->
      if (group.name == groupName)
      {
         groupType = group.type
         discordID = group.discordID
      }
   }

    def tagTemplate = '';

    switch(groupType.toLower()) {
        case 'user':
            tagTemplate = '<@[id]>';
            break;
        case 'role':
            tagTemplate = '<@&[id]}>';
            break;
        case 'channel':
            tagTemplate = '<#[id]>';
            break;
        default:
            log.error("Tried to mention group '${groupName}', but this group has an invalid type '${groupType}'!");
            return;
            break;
    }

    def mentionString = '';

     if(discordID?.trim()) {
         mentionString += "${tagTemplate.replace('[id]', discordID)}, ";
     }
   
    // Not needed anymore?
    Remove trailing ', '
    message = message.substring(0, message.length() - 2);

    message = "${groupName}: ${message}";
    return message;
}

def mentionGroups(groups, List<String> groupNames) {
   def message = '';

   for(name in groupNames) {
      message += "${mentionGroup(groups, name)}\n";
   }

   return message;
}

def swarmIDToDiscordID(groups, swarmID) {
    def swarmName = swarmID.replace('\\d', '');

    for(group in groups) {
        if(group.name == swarmName) {
            if(group.members.size() > 0) {
                return group.members[0];
            }
        }
    }

    log.error("Unable to find Discord ID for Swarm ID ${swarmID}!");
    return '';
}

def loadGroups(String groupFilePath) {
    def content = readFile(file: groupFilePath).split();

    def groups = []
    for(obj in content) {
        groups += [new JsonSlurper().parseText(obj)];
    }
    return groups;
}

def saveGroups(groups, String outGroupFilePath) {
    file.write(outGroupFilePath, JsonOutput.toJson(groups));
}
