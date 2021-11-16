package ai.digital.integration.server.common.domain

import org.gradle.api.model.ObjectFactory

class Provider(objects: ObjectFactory) {
    var provisioner: String = "terraform"
    var eksClusterName: String = "Integration-Server-Plugin-EKS"
    var eksVpcName: String = "Integration-Server-Plugin-VPC"
    var eksVpcSource: String = "terraform-aws-modules/vpc/aws"
    var eksVpcVersion: String = "2.78.0"
    var eksSource: String = "terraform-aws-modules/eks/aws"
    var eksVersion: String = "17.18.0"
    var eksClusterVersion: String = "1.17"
}
