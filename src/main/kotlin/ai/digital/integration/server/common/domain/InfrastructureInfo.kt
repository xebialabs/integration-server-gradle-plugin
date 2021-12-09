package ai.digital.integration.server.common.domain

open class InfrastructureInfo(val clusterName: String?,
                              val userName: String?,
                              val apiServerURL: String?,
                              val caCert: String?,
                              val tlsCert: String?,
                              val tlsPrivateKey: String?)
