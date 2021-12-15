package ai.digital.integration.server.common.constant

enum class OperatorProviderName(val providerName: String) {
    AWS_EKS("aws-eks"),
    AWS_OPENSHIFT("aws-openshift"),
    AZURE_AKS("azure-aks"),
    GCP_GKE("gcp-gke"),
    ON_PREMISE("onprem"),
    VMWARE_OPENSHIFT("vmware-openshift"),
}
