pipeline {
  agent any
  environment {
    // fallback list: the 10 jobs we created
    JOB_LIST = "Team-01/ci-cd-pipeline,Team-02/ci-cd-pipeline,Team-03/ci-cd-pipeline,Team-04/ci-cd-pipeline,Team-05/ci-cd-pipeline,Team-06/ci-cd-pipeline,Team-07/ci-cd-pipeline,Team-08/ci-cd-pipeline,Team-09/ci-cd-pipeline,Team-10/ci-cd-pipeline"
  }

  stages {
    stage('Prepare job list') {
      steps {
        script {
          def jobs = []
          if (fileExists('jobs.txt')) {
            jobs = readFile('jobs.txt').split('\\n').collect{ it.trim() }.findAll{ it }
            echo "Loaded jobs from jobs.txt (${jobs.size()})"
          } else if (env.JOB_LIST?.trim()) {
            jobs = env.JOB_LIST.split(',').collect{ it.trim() }.findAll{ it }
            echo "Loaded jobs from JOB_LIST (${jobs.size()})"
          } else {
            error "No jobs found. Provide a jobs.txt in workspace or set JOB_LIST."
          }
          env.TARGETS = jobs.join(',')
        }
      }
    }

    stage('Trigger pipelines (parallel)') {
      steps {
        script {
          def jobs = readFile('jobs.txt').split('\n').collect { it.trim() }.findAll { it }
          echo "Will trigger ${jobs.size()} jobs in parallel (or as agent capacity allows)."

          def branches = [:]
          def summary = Collections.synchronizedList([])

          jobs.each { jobName ->
            // each key must be a String (branch name)
            branches[jobName] = {
              try {
                echo "Triggering ${jobName}"
                // if your downstream jobs require parameters, add parameters: [...]
                def b = build job: jobName, wait: true, propagate: false
                def r = b?.result ?: 'UNKNOWN'
                def d = b?.duration ?: 0
                summary << [name: jobName, result: r, duration: d]
                echo "${jobName} => ${r}"
              } catch (err) {
                echo "Error triggering ${jobName}: ${err}"
                summary << [name: jobName, result: "ERROR: ${err}", duration: 0]
              }
            }
          }

          // run in parallel
          parallel branches

          // Create CSV
          def csv = 'Pipeline,Result,Duration_ms\\n'
          summary.each { s -> csv += "${s.name},${s.result},${s.duration}\\n" }
          writeFile file: 'pipeline_results.csv', text: csv

          // Create simple HTML report
          def html = """<html><head><title>Pipeline Test Report</title></head><body>
            <h2>Jenkins Pipeline Validation Report</h2>
            <table border='1' cellpadding='5' cellspacing='0'>
              <tr><th>Pipeline</th><th>Status</th><th>Duration (ms)</th></tr>"""
          summary.each { s ->
            def color = (s.result == 'SUCCESS') ? 'green' : 'red'
            html += "<tr><td>${s.name}</td><td style='color:${color}'>${s.result}</td><td>${s.duration}</td></tr>"
          }
          html += "</table></body></html>"
          writeFile file: 'pipeline_report.html', text: html

          archiveArtifacts artifacts: 'pipeline_report.html,pipeline_results.csv'
        }
      }
    }
  }

  post {
    always {
      echo "Validation run finished. Reports archived in build artifacts."
    }
  }
}
