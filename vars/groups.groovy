import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def createGroup(String groupName, List groupMembers) {
   return JsonOutput.toJson([
      name: groupName,
      members: groupMembers
   ]);
}

def getGroupMembers(groups, String groupName) {
    for(group in groups) {
        if(group.name == groupName) {
            return group.members;
        }
    }

    log.error("Tried to get group members for group '${groupName}', but this group couldn't be found!");
    return [];
}

def getGroupType(groups, String groupName) {
    for(group in groups) {
        if(group.name == groupName) {
            return group.type;
        }
    }

    log.error("Tried to get group type for group '${groupName}', but this group couldn't be found!");
    return [];
}

def mentionGroup(groups, String groupName) {
    def groupMembers = getGroupMembers(groups, groupName);
    def groupType = getGroupType(groups, groupName);

    if(!groupMembers && !groupType) {
        return '';
    }

    def tagTemplate = '';

    switch(groupType.toLower()) {
        case 'user':
            tagTemplate = '<@{id}>';
            break;
        case 'role':
            tagTemplate = '<@&${id}>';
            break;
        case 'channel':
            tagTemplate = '<#{id}>';
            break;
        default:
            log.error("Tried to mention group '${groupName}', but this group has an invalid type '${groupType}'!");
            return;
            break;
    }

    def mentionString = '';

    for(member in groupMembers) {
        discordID = member.value;

        if(discordID?.trim()) {
            mentionString += "${tagTemplate.replace('{id}', discordID)}, ";
        }
    }

    // Remove trailing ', '
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
    def content = readFile(file: groupFilePath);
    return (new JsonSlurper().parseText(content));
}

def saveGroups(groups, String outGroupFilePath) {
    file.write(outGroupFilePath, JsonOutput.toJson(groups));
}
