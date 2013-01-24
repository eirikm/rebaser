package rebaser

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
      case None => explodeFirstCommit(commit)
      case Some(parentCommit) => explodeOtherCommit(parentCommit)
    }
  }

  private def explodeFirstCommit(firstCommit: RevCommit): Option[RevCommit] =
    None // TODO treat first commit special

  private def explodeOtherCommit(parentCommit: RevCommit): Option[RevCommit] = {
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

    val addedFiles: mutable.Set[String] = asScalaSet(status.getAdded())
    val changedFiles: mutable.Set[String] = asScalaSet(status.getChanged())
    val removedFiles: mutable.Set[String] = asScalaSet(status.getRemoved())

    // unstage all files
    (addedFiles ++ changedFiles ++ removedFiles).foreach(unstageFile)

    val modifiedFiles: mutable.Set[String] = asScalaSet(status.getModified())

    // stage one file at a time and commit
    addedFiles foreach {
      filename =>
        git.add().addFilepattern(filename).call()

        val lines: Array[String] = originalCommitMessage.split('\n')
        // TODO use string interpolation
        lines.update(0, lines.head + " (add " + filename + ")")
        git.commit().setMessage(lines.mkString("\n")).call()
    }
    // TODO commit each changed file
    // TODO commit each removed file
    // TODO what about moved files?

    git.rebase().setOperation(RebaseCommand.Operation.CONTINUE).call()

    Some(getHeadCommit)
  }

  def unstageFile(filename: String) = {
    git.reset().addPath(filename).call()
  }
}




