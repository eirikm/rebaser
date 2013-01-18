import java.io.File
import java.util
import org.eclipse.jgit.api.RebaseCommand.Action
import org.eclipse.jgit.api.{RebaseResult, RebaseCommand, Git}
import org.eclipse.jgit.api.RebaseResult.Status
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import org.eclipse.jgit.lib.RepositoryTestCase
import org.eclipse.jgit.revwalk.RevCommit
import org.junit.Test


import org.hamcrest.CoreMatchers.is
import org.junit.Assert._
import org.eclipse.jgit.diff.DiffEntry.DEV_NULL
import scala.Predef.String

class RebaserTest extends RepositoryTestCase {

  case class GitFile(name: String, content: String)

  type CommitMessage = String


  @Test
  def diffInitialCommit() {
    // arrange
    implicit val git = new Git(db);
    val rebaser: Rebaser = new Rebaser(db)

    val commit: RevCommit = createAddAndCommitFile(GitFile("a.txt", "content"), "initial commit")

    // act
    val result: util.List[DiffEntry] = rebaser.getAffectedFiles(commit)

    // assert
    assertThat(Integer.valueOf(result.size()), is(Integer.valueOf(1)));
    val entry: DiffEntry = result.get(0);
    assertThat(entry.getChangeType(), is(ChangeType.ADD));
    assertThat(entry.getNewPath(), is("a.txt"));
    assertThat(entry.getOldPath(), is(DEV_NULL));
  }


  @Test
  def diffNormalCommit() {
    // arrange
    implicit val git = new Git(db);
    val rebaser: Rebaser = new Rebaser(db)

    createAddAndCommitFile(GitFile("a.txt", "content"), "commit a")
    val commit = createAddAndCommitFile(GitFile("b.txt", "content"), "commit b")

    // act
    val result: util.List[DiffEntry] = rebaser.getAffectedFiles(commit)

    // assert
    assertThat(Integer.valueOf(result.size()), is(Integer.valueOf(1)));
    val entry: DiffEntry = result.get(0);
    assertThat(entry.getChangeType(), is(ChangeType.ADD));
    assertThat(entry.getNewPath(), is("b.txt"));
    assertThat(entry.getOldPath(), is(DEV_NULL));
  }

  @Test
  def testRebaseInteractiveReword {
    // arrange
    implicit val git = new Git(db);
    val rebaser: Rebaser = new Rebaser(db)

    createAddAndCommitFile(GitFile("file1", "content for file1"), "Add file1")

    createAddAndCommitFile(GitFile("file2", "content for file2"), "Add file2")

    createAddAndCommitFile(GitFile("file1", "updated content for file1"), "Updated file1 on master")

    createAddAndCommitFile(GitFile("file2", "updated content for file2"), "updated file2 on side")

    val res: RebaseResult = git.rebase().setUpstream("HEAD~2").runInteractively(new RebaseCommand.InteractiveHandler {
      def prepareSteps(steps: util.List[RebaseCommand.Step]) {
        steps.get(0).setAction(Action.REWORD)
      }

      def modifyCommitMessage(commit: String): String = {
        return "rewritten commit message"
      }
    }).call

    assertTrue(new java.io.File(db.getWorkTree(), "file2").exists());
    checkFile(new java.io.File(db.getWorkTree(), "file2"), "updated content for file2");
    assertEquals(Status.OK, res.getStatus());
    val logIterator: util.Iterator[RevCommit] = git.log().all().call().iterator();
    logIterator.next(); // skip first commit;
    val actualCommitMag: String = logIterator.next().getShortMessage();
    assertEquals("rewritten commit message", actualCommitMag);
  }

  def createAddAndCommitFile(file: GitFile, commitMsg: CommitMessage)(implicit git: Git): RevCommit = {
    writeTrashFile(file.name, file.content);
    git.add().addFilepattern(file.name).call();
    git.commit().setMessage(commitMsg).call()
  }
}
