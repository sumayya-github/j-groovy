job("Groovy 1") 
     {
	description("This is the first job of groovy project")
	keepDependencies(false)
	scm {
		git {
			remote {
				github("sumayya-github/j-groovy", "https")
			}
			branch("*/master")
		}
	}
	disabled(false)
	triggers {
		scm("* * * * *") {
			ignorePostCommitHooks(false)
		}
	}
	concurrentBuild(false)
	steps {
		shell('sudo cp -r -v -f * /t3')
	}
}

job("Groovy 2")
{
description ("This is my second job for Groovy project ")
steps{
shell("""
if sudo kubectl get all | grep apache
then
echo "PODS EXISTS,GOING TO DELETE IT"
sudo kubectl delete all --all
sudo kubectl delete apache-pv-claim1
else
echo "POD DOES NOT EXISTS,GOING TO CREATE IT"
sudo kubectl create -f /t3/apache_svc.yml
sudo kubectl create -f /t3/apache_pvc.yml
sudo kubectl create -f /t3/apache_deploy.yml
fi 
sudo kubectl get all """)
}
triggers {
   upstream('Groovy 1', 'SUCCESS')
     }
  }
job("Groovy 3") {
	description("This is the third job of groovy project")
	
	triggers {
	        
	        upstream {
	            upstreamProjects('Groovy 2')
	            threshold('SUCCESS')
	        }
	    }
	steps {
		shell("""status=\$(curl -sL -w "%{http_code}" -I "http://192.168.99.100:30007" -o /dev/null)
if [[ \$status == 200 ]]
then
exit 0
else
exit 1
fi""")
}
	
job("Groovy 4")
{
description ("This is mailing job")
 authenticationToken('mail')
   publishers {
		mailer("sumayyakhatoon58@gmail.com", false, false)
	}
   triggers {
   upstream('Groovy 3', 'SUCCESS')
   }
   }
buildPipelineView('Groovy Project') {
    filterBuildQueue()
    filterExecutors()
    title('Groovy Pipeline Complete View')
    displayedBuilds(3)
    selectedJob('Groovy 1')
    alwaysAllowManualTrigger()
    showPipelineParameters()
    refreshFrequency(60)
    }
}
