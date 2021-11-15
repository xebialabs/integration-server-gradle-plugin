package ai.digital.integration.server.common.domain

class Terraform(val name: String) {

    var eksClusterName = "Integration-Server-Plugin-EKS"
    var eksVpcName = "Integration-Server-Plugin-VPC"
    var eksVpcSource = "terraform-aws-modules/vpc/aws"
    var eksVpcVersion = "2.78.0"
    var eksSource = "terraform-aws-modules/eks/aws"
    var eksVersion = "17.18.0"
    var eksClusterVersion = "1.17"

}