package ai.digital.integration.server.domain

class DevOpsAsCode {
    File devOpAsCodeScript
    String name
    String scmAuthor
    String scmCommit
    String scmDate
    String scmFile
    String scmMessage
    String scmRemote
    String scmType

    DevOpsAsCode(final String name) {
        this.name = name
    }
}
