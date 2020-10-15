import groovy.json.JsonSlurper

class SwarmConfig {
    static String swarmUser = '';
    static String swarmUrl = '';
    static String p4Ticket = '';
}

def init(String swarmUser, String swarmUrl, String p4Ticket) {
    SwarmConfig.swarmUser = swarmUser;
    SwarmConfig.swarmUrl = swarmUrl;
    SwarmConfig.p4Ticket = p4Ticket;
}

def clear() {
    SwarmConfig.swarmUser = '';
    SwarmConfig.swarmUrl = '';
    SwarmConfig.p4Ticket = '';
}

def getGroupMembers(groups, String groupName) {
    return groups.getGroupMembers(groups, groupName).keySet() as String[];
}

def getGroupMembers(groups, List<String> groupNames) {
    def members = [];

    for(name in groupNames) {
        members += getGroupMembers(groups, name);
    }
    
    return members;
}

def createReview(changeID, List participants = []) {
    def reviewers = '';
    for(p in participants) {
        reviewers += "-d \"reviewers[]=${p} \"";
    }

    def output = bat(label: 'Create Swarm Review', returnStdout: true, script: "curl -u \"${SwarmConfig.swarmUser}:${SwarmConfig.p4Ticket}\" -X POST -d \"change=${changeID}\" \"${reviewers}\" \"${SwarmConfig.swarmUrl}/api/v9/reviews/\"");
    def responseArray = output.split('\\n');
    return responseArray[2].trim();
}

def getReviewID(String curlResponse) {
    def reviewInfo = new JsonSlurper().parseText(curlResponse);
    return reviewInfo.review.id
}

def getReviewAuthor(String curlResponse) {
   def reviewInfo = new JsonSlurper().parseText(curlResponse);
   return reviewInfo.review.author;
}

def upvoteReview(reviewID) {
   bat(label: "Upvote Swarm Review", script: "curl -u \"${SwarmConfig.swarmUser}:${SwarmConfig.p4Ticket}\" -X POST \"${SwarmConfig.swarmUrl}/reviews/${reviewID}/vote/up\"");
}

def downvoteReview(reviewID) {
   bat(label: "Upvote Swarm Review", script: "curl -u \"${SwarmConfig.swarmUser}:${SwarmConfig.p4Ticket}\" -X POST \"${SwarmConfig.swarmUrl}/reviews/${reviewID}/vote/down\"");
}

def needsReview(reviewID) {
   setReviewState(reviewID, "needsReview");
}

def needsRevision(reviewID) {
   setReviewState(reviewID, "needsRevision");
}

def approve(reviewID) {
   setReviewState(reviewID, "approved");
}

def archive(reviewID) {
   setReviewState(reviewID, "archived");
}

def reject(reviewID) {
   setReviewState(reviewID, "rejected");
}

def setReviewState(reviewID, String state) {
   bat(label: "Set Swarm Review State", script: "curl -u \"${SwarmConfig.swarmUser}:${SwarmConfig.p4Ticket}\" -X PATCH -d \"state=${state}\" \"${SwarmConfig.swarmUrl}/api/v9/reviews/${reviewID}/state/\"");
}
