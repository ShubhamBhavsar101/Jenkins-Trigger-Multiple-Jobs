import jenkins.model.*
import com.cloudbees.hudson.plugins.folder.*

// === Get Jenkins root instance ===
def jenkins = Jenkins.instance

// === Define your job paths (with folders) ===
def jobPaths = [
    "Team-01/ci-cd-pipeline",
    "Team-02/ci-cd-pipeline",
    "Team-03/ci-cd-pipeline",
    "Team-04/ci-cd-pipeline",
    "Team-05/ci-cd-pipeline",
    "Team-06/ci-cd-pipeline",
    "Team-07/ci-cd-pipeline",
    "Team-08/ci-cd-pipeline",
    "Team-09/ci-cd-pipeline",
    "Team-10/ci-cd-pipeline"
]

// === XML template for a simple freestyle job ===
def xmlTemplate = { jobName ->
    return """<?xml version='1.1' encoding='UTF-8'?>
<project>
  <actions/>
  <description>Auto-created via Jenkins Script Console</description>
  <keepDependencies>false</keepDependencies>
  <properties/>
  <scm class="hudson.scm.NullSCM"/>
  <canRoam>true</canRoam>
  <disabled>false</disabled>
  <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
  <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
  <triggers/>
  <builders>
    <hudson.tasks.Shell>
      <command>echo "Running ${jobName}"</command>
    </hudson.tasks.Shell>
  </builders>
  <publishers/>
  <buildWrappers/>
</project>
"""
}

// === Helper: create folders recursively if needed ===
def getOrCreateFolder(String folderPath) {
    def parts = folderPath.split('/')
    def current = Jenkins.instance
    def currentPath = ""
    parts.each { part ->
        currentPath = currentPath ? "${currentPath}/${part}" : part
        def existing = current.getItem(part)
        if (existing == null) {
            println "📁 Creating folder: ${currentPath}"
            existing = current.createProject(Folder.class, part)
        }
        current = existing
    }
    return current
}

// === Main logic: Create folders + jobs ===
jobPaths.each { fullPath ->
    def parts = fullPath.split('/')
    def jobName = parts[-1]
    def folderPath = parts[0..-2].join('/')
    def parent = folderPath ? getOrCreateFolder(folderPath) : jenkins

    if (parent.getItem(jobName)) {
        println "✅ Job already exists: ${fullPath}"
    } else {
        println "🚀 Creating job: ${fullPath}"
        def xml = xmlTemplate(fullPath)
        def stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))
        parent.createProjectFromXML(jobName, stream)
        println "✅ Created successfully: ${fullPath}"
    }
}

println "\n🎉 All jobs (and folders) created successfully!"
