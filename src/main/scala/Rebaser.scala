import java.util
import org.eclipse.jgit.api.RebaseCommand.Action
import org.eclipse.jgit.api.{RebaseCommand, RebaseResult, Git}
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.revwalk.{RevWalk, RevCommit}
import org.eclipse.jgit.treewalk.{EmptyTreeIterator, TreeWalk}

class Rebaser(git: Git) {
  def getAffectedFiles(commit: RevCommit): java.util.List[DiffEntry] = {
    val walk: TreeWalk = new TreeWalk(git.getRepository);

    commit.getParentCount match {
      case 0 => walk.addTree(new EmptyTreeIterator())
      case 1 => walk.addTree(getParentCommit(commit).getTree)
      case _ => println("This should not happen")
    }
    walk.addTree(commit.getTree())
    DiffEntry.scan(walk)
  }

  def rewordCommit(commitToReword: RevCommit, commitMessage: String): RebaseResult = {
    val parentCommit: RevCommit = getParentCommit(commitToReword)
    git.rebase().setUpstream(parentCommit).runInteractively(new RebaseCommand.InteractiveHandler {
      def prepareSteps(steps: util.List[RebaseCommand.Step]) {
        steps.get(0).setAction(Action.REWORD)
      }

      def modifyCommitMessage(commit: String): String = {
        return commitMessage
      }
    }).call
  }

  def getParentCommit(commit: RevCommit) = {
    val rw = new RevWalk(git.getRepository)
    rw.parseCommit(commit.getParent(0).getId)
  }
}

object Rebaser extends App {
}
