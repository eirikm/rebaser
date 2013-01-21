import java.lang.Iterable
import java.util
import org.eclipse.jgit.api.RebaseCommand.{Step, Action}
import org.eclipse.jgit.api.{RebaseCommand, RebaseResult, Git}
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.{RevTree, RevWalk, RevCommit}
import org.eclipse.jgit.treewalk.{EmptyTreeIterator, TreeWalk}
import scala.collection.JavaConversions._


class Rebaser(val git: Git) extends GitUtilityMethods with RewordCommit with SwapCommit {

  def getAffectedFiles(commit: RevCommit): java.util.List[DiffEntry] = {
    val walk: TreeWalk = new TreeWalk(git.getRepository)

    getParentCommit(commit) match {
      case None => walk.addTree(new EmptyTreeIterator())
      case Some(parentCommit) => walk.addTree(parentCommit.getTree)
    }

    walk.addTree(commit.getTree())
    DiffEntry.scan(walk)
  }

}

trait RewordCommit extends GitUtilityMethods {

  val git: Git

  // TODO should this return new HEAD instead of RebaseResult?
  def rewordCommit(commitToReword: RevCommit, commitMessage: String): RebaseResult = {
    getParentCommit(commitToReword) match {
      case None => rewordFirstCommit(commitToReword, commitMessage)
      case Some(parentCommit) => rewordOtherCommit(commitToReword, commitMessage, parentCommit)
    }
  }

  // waiting for jgit to support rebase --onto before finishing development on this feature
  private def rewordFirstCommit(commit: RevCommit, commitMessage: String): RebaseResult = {
    val tmpBranchName: String = "tmp_first_commit_branch"
    val currentBranch: String = "master"

    git.branchCreate().setName(tmpBranchName).setStartPoint(commit).call()
    git.checkout().setName(tmpBranchName).call()
    git.commit().setAmend(true).setMessage(commitMessage).call
    git.checkout().setName(currentBranch).call()

    //    val logIterator: Iterable[RevCommit] = git.log().all().call()
    //    val secondCommit: RevCommit = logIterator.take(2).toIndexedSeq.get(1)

    git.rebase().setUpstream(tmpBranchName).call()
  }

  private def rewordOtherCommit(commitToReword: RevCommit, commitMessage: String, parentCommit: RevCommit): RebaseResult = {
    git.rebase().setUpstream(parentCommit).runInteractively(new RebaseCommand.InteractiveHandler {
      def prepareSteps(steps: util.List[RebaseCommand.Step]) {
        steps.get(0).setAction(Action.REWORD)
      }

      def modifyCommitMessage(commit: String): String = {
        return commitMessage
      }
    }).call
  }
}

trait SwapCommit extends GitUtilityMethods {
  // TODO return new HEAD commit instead of rebase result
  def swapWithNextCommit(commit: RevCommit): RebaseResult = {
    getParentCommit(commit) match {
//      case None =>
      case Some(parentCommit) => {
        git.rebase().setUpstream(parentCommit).runInteractively(new RebaseCommand.InteractiveHandler {
          def prepareSteps(steps: util.List[Step]) {
            // swap step 0 and step 1
            val tmp = steps.get(0)
            steps.set(0, steps.get(1))
            steps.set(1, tmp)
          }

          def modifyCommitMessage(commit: String) = commit
        }).call()
      }
    }
  }
}

trait GitUtilityMethods {

  val git: Git

  def getParentCommit(commit: RevCommit): Option[RevCommit] = {
    commit.getParentCount match {
      case 0 => None
      case _ => {
        val rw = new RevWalk(git.getRepository)
        val firstCommitId: ObjectId = commit.getParent(0).getId
        Some(rw.parseCommit(firstCommitId))
      }
    }
  }
}

object Rebaser extends App {
}
