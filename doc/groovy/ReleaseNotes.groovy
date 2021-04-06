import groovy.json.*
import java.time.*


if (args.length < 2){
    println """Usage: 
\$groovy ReleaseNotes.groovy olderTag newerTag
"""
    System.exit(-1)
}

/**
 * Makes an HTTP call and returns a content of resource
 *
 * @param location - the URL of the resource
 * @return content of resource
 */
def fetch(location) {
    def url = new URL(location)
    def connection = url.openConnection()
    connection.setRequestProperty("Authorization", "token ${System.getenv('JAVALITE_GITHUB_TOKEN')}")
    connection.requestMethod = 'GET'
    try{
        return connection.content.text
    }catch(FileNotFoundException e){
        println "Failed to find resource: $location"
        System.exit(-1)
    }
}

/**
 *  Gets  an instance of LocalDateTime for tag
 *
 * @param tag tag to get
 * @return
 */
def getTagDate(tag){
    tagsLocation = "https://api.github.com/repos/javalite/javalite/git/refs/tags/${tag}"
    response = fetch(tagsLocation)
    tagLocation = new JsonSlurper().parseText(response).object.url
    response = fetch(tagLocation)
    date = new JsonSlurper().parseText(response).tagger.date
    return ZonedDateTime.parse(date).toLocalDateTime()
}


def getNextPage(index) {
    json = fetch("https://api.github.com/repos/javalite/javalite/issues?state=closed&page=${index}&sort=created")
    return new JsonSlurper().parseText(json)
}


def  checkModuleLabel(issue) {
    boolean hasModule = false
    issue.labels.each { label ->
        if(label.name.contains("Module")){
            hasModule = true
        }
    }
    if(!hasModule){
        println ">>>>>>>>>>>>>> MISSING MODULE LABEL!!! >>>>>>> #$issue.number - $issue.closed_at - $issue.title / Labels: $issue.labels.name"
    }
}



tag1 = args[0]
tag2 = args[1]

tag1Date = getTagDate(tag1)
tag2Date = getTagDate(tag2)

println "Getting issues between  $tag1 / $tag1Date and $tag2 / $tag2Date"

List allIssues = new ArrayList()

for (int i = 1; true; i++) {
    issues = getNextPage(i)
    if (issues.size() > 0) {
        allIssues.addAll(issues)
    } else {
        break
    }
}

class SortClosedAt implements Comparator{
    @Override
    int compare(Object iss1, Object iss2) {
        return iss1.closed_at.compareTo(iss2.closed_at)
    }
}

Collections.sort(allIssues, new SortClosedAt());

//key: label that has a 'Module' in a name
modulesMap = new HashMap()

allIssues.each{issue ->
    issueTime = ZonedDateTime.parse(issue.closed_at).toLocalDateTime()
    if(issueTime.compareTo(tag1Date) > 0 && issueTime.compareTo(tag2Date) < 0){

        if(issue.containsKey("pull_request")) { // we do not want pull requests
//            println "Skipping pull request ${issue.number}"
           return // next iteration, this is a closure
        }

        checkModuleLabel(issue)

        issue.labels.each { label ->
            if(label.name.contains("Module")){
                if(!modulesMap.containsKey(label.name)){
                    List issues = new ArrayList()
                    modulesMap.put(label.name, issues)
                }
                modulesMap.get(label.name).add(issue)
            }
        }
    }
}

modulesMap.keySet().each{ label ->

    println "#### $label"
    modulesMap.get(label).each{ issue ->
        println "#$issue.number - $issue.title /  $issue.labels.name"
    }
}

