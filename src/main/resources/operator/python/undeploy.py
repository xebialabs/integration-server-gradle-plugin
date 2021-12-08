
def undeploy(ci):
    if repository.exists(ci):
        print("Undeploying " + ci)
        ci_undeploy_task = deployment.createUndeployTask(ci)
        deployit.startTaskAndWait(ci_undeploy_task.id)
    else:
        print("Skip undeploy of " + ci + ", already exists")

xld_cr = "Environments/kubernetes-envs/xld/xld-cr"
xld_operator_app = "Environments/kubernetes-envs/xld/xld-operator-app"

undeploy(xld_cr)
undeploy(xld_operator_app)
