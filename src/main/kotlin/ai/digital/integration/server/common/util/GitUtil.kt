package ai.digital.integration.server.common.util

import java.nio.file.Path
import java.nio.file.Paths

class GitUtil {
    companion object {
        fun checkout(repo: String, destinationPath: Path, branch: String? = null): Path {
            val dest = Paths.get(destinationPath.toAbsolutePath().toString(), repo)

            // it needs to be aligned with operatorImage default value
            val branchClone = if (branch != null) "-b $branch" else ""
            ProcessUtil.executeCommand(
                    "rm -fr \"${dest.toAbsolutePath()}\"; git clone git@github.com:xebialabs/$repo.git \"${dest.toAbsolutePath()}\" $branchClone")
            return dest
        }
    }
}
