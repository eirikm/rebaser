import collection.mutable
import java.util
import org.eclipse.jgit.api.RebaseCommand.{InteractiveHandler, Step, Action}
import org.eclipse.jgit.api._
import org.eclipse.jgit.revwalk.RevCommit
import scala.collection.JavaConversions._
import scala.Some

trait ExplodeCommit extends GitUtilityMethods {

  def explodeCommit(commit: RevCommit): Option[RevCommit] = {
    getParentCommit(commit) match {
      case None => None // TODO treat first commit special
      case Some(parentCommit) => {
        val result: RebaseResult = git.rebase().setUpstream(parentCommit).runInteractively(new InteractiveHandler {
          def prepareSteps(steps: util.List[Step]) {
            steps.get(0).setAction(Action.EDIT)
          }

          def modifyCommitMessage(commit: String) = commit
        }).call()

        println(result.getStatus)
        println(result.getCurrentCommit.getName)
        val originalCommitMessage: String = result.getCurrentCommit.getFullMessage
        println(originalCommitMessage)

        git.reset().setMode(ResetCommand.ResetType.SOFT).setRef("HEAD^").call()

        val status: Status = git.status().call()

        // TODO unstage all modified files
        // unstage all files
        val addedFiles: mutable.Set[String] = asScalaSet(status.getAdded())
        addedFiles foreach unstageFile

        // TODO unstage modified
        // TODO unstage deleted

        status.getChanged

        // stage one file at a time and commit
        addedFiles foreach {
          filename =>
            git.add().addFilepattern(filename).call()
            git.commit().setMessage(originalCommitMessage).call()
        }

        git.rebase().setOperation(RebaseCommand.Operation.CONTINUE).call()

        Some(getHeadCommit)
      }
    }
  }

  def unstageFile(filename: String) = {
    git.reset().addPath(filename).call()
  }
}




