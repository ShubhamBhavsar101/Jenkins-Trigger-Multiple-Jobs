pipeline {
  agent any

  environment {
    // Optional fallback in case jobs.txt is missing
    JOB_LIST = "Team-01/ci-cd-pipeline,Team-02/ci-cd-pipeline,Team-03/ci-cd-pipeline,Team-04/ci-cd-pipeline,Team-05/ci-cd-pipeline,Team-06/ci-cd-pipeline,Team-07/ci-cd-pipeline,Team-08/ci-cd-pipeline,Team-09/ci-cd-pipeline,Team-10/ci-cd-pipeline"
  }

  stages {
    stage('Checkout Repo') {
      steps {
        // Pull the repo that contains jobs.txt
        checkout scm
      }
    }

    stage('Load Job List') {
      steps {
        script {
          def jobs = []
          if (fileExists('jobs.txt')) {
            jobs = readFile('jobs.txt').split('\n').collect { it.trim() }.findAll { it }
            echo "✅ Loaded ${jobs.size()} jobs from jobs.txt"
          } else if (env.JOB_LIST?.trim()) {
            echo "⚠️ jobs.txt not found — using fallback JOB_LIST"
            jobs = env.JOB_LIST.split(',').collect { it.trim() }.findAll { it }
          } else {
            error "❌ No jobs found. Add a jobs.txt file in your GitHub repo or define JOB_LIST."
          }
          env.TARGETS = jobs.join(',')
          echo "Job targets: ${env.TARGETS}"
        }
      }
    }

    stage('Trigger Pipelines (Parallel)') {
      steps {
        script {
          def jobs = env.TARGETS.split(',').collect { it.trim() }.findAll { it }
          echo "🚀 Will trigger ${jobs.size()} pipelines in parallel."

          def branches = [:]
          def summary = Collections.synchronizedList([])

          jobs.each { jobName ->
            branches[jobName] = {
              try {
                echo "▶️ Triggering ${jobName}"
                def b = build job: jobName, wait: true, propagate: false
                def r = b?.result ?: 'UNKNOWN'
                def d = b?.duration ?: 0
                summary << [name: jobName, result: r, duration: d]
                echo "${jobName} finished with status ${r}"
              } catch (err) {
                echo "❌ Error triggering ${jobName}: ${err}"
                summary << [name: jobName, result: "ERROR", duration: 0]
              }
            }
          }

          // Run all jobs in parallel
          parallel branches

          // === Create CSV Summary ===
          def csv = "Pipeline,Result,Duration_ms\n"
          summary.each { s -> csv += "${s.name},${s.result},${s.duration}\n" }
          writeFile file: 'pipeline_results.csv', text: csv

          // === Create HTML Summary ===
          def html = """<html><head><title>Pipeline Validation Report</title></head><body>
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
      echo "🏁 Validation run finished. Reports archived in build artifacts."
    }
  }
}
