
def undeploy(ci):
    if repository.exists(ci):
        print("Undeploying " + ci)
        ci_undeploy_task = deployment.createUndeployTask(ci)
        deployit.startTaskAndWait(ci_undeploy_task.id)
    else:
        print("Skip undeploy of " + ci + ", CI does not exist")

xl_release_deployment = "Environments/XEBIALABS/K8S/XL-Release-Deployment"
xl_deploy_deployment = "Environments/XEBIALABS/K8S/XL-Deploy-Deployment"
answers_configmap_deployment = "Environments/XEBIALABS/K8S/Answers-Configmap-Deployment"
k8s_deployment = "Environments/XEBIALABS/K8S/K8s-NameSpace"

undeploy(xl_release_deployment)
undeploy(xl_deploy_deployment)
answer_deployment = repository.read(answers_configmap_deployment)
answer_deployment.undeployDependencies = 'true'
repository.update(answer_deployment)
undeploy(answers_configmap_deployment)
undeploy(k8s_deployment)
