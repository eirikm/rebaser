import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.RepositoryTestCase
import org.eclipse.jgit.revwalk.RevCommit

abstract class AbstractRebaserTest extends RepositoryTestCase {
  case class GitFile(name: String, content: String)

  type CommitMessage = String

  def createAddAndCommitFile(file: GitFile, commitMsg: CommitMessage)(implicit git: Git): RevCommit= {
    writeTrashFile(file.name, file.content);
    git.add().addFilepattern(file.name).call();
    git.commit().setMessage(commitMsg).call()
  }
}