
def undeploy(ci):
    if repository.exists(ci):
        print("Undeploying " + ci)
        ci_undeploy_task = deployment.createUndeployTask(ci)
        deployit.startTaskAndWait(ci_undeploy_task.id)
    else:
        print("Skip undeploy of " + ci + ", CI does not exist")

xlr_cr = "Environments/kubernetes-envs/xlr/xlr-cr"
xlr_operator_app = "Environments/kubernetes-envs/xlr/xlr-operator-app"

undeploy(xlr_cr)
undeploy(xlr_operator_app)
