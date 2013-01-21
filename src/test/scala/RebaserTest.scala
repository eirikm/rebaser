import collection.mutable
import java.util
import org.eclipse.jgit.api.RebaseCommand.{Step, Action}
import org.eclipse.jgit.api.RebaseResult.Status
import org.eclipse.jgit.api.{RebaseResult, RebaseCommand, Git}
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import org.eclipse.jgit.lib.RepositoryTestCase
import org.eclipse.jgit.revwalk.RevCommit
import org.junit.{Ignore, Test}


import org.hamcrest.CoreMatchers.is
import org.junit.Assert._
import org.eclipse.jgit.diff.DiffEntry.DEV_NULL
import scala.Predef.String

import scala.collection.JavaConversions._

class RebaserTest extends RepositoryTestCase {

  case class GitFile(name: String, content: String)

  type CommitMessage = String


  @Test
  def diffInitialCommit() {
    // arrange
    implicit val git = new Git(db);
    val rebaser: Rebaser = new Rebaser(git)

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
    val rebaser: Rebaser = new Rebaser(git)

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
  def rewordSecondCommit {
    // arrange
    implicit val git = new Git(db);
    val rebaser: Rebaser = new Rebaser(git)

    val commit1Msg: String = "Add file1"
    val commit2Msg: String = "Add file2"
    val commit3Msg: String = "Add file3"
    val c1 = createAddAndCommitFile(GitFile("file1", "content for file1"), commit1Msg)
    val c2 = createAddAndCommitFile(GitFile("file2", "content for file2"), commit2Msg)
    val c3 = createAddAndCommitFile(GitFile("file3", "content for file3"), commit3Msg)

    // act
    val rewordedCommitMessage: String = "reworded commit message"
    val res = rebaser.rewordCommit(c2, rewordedCommitMessage)

    // assert
    //    assertEquals(Status.OK, res.getStatus());
    val logIterator: util.Iterator[RevCommit] = git.log().all().call().iterator();
    logIterator.next();
    // skip first commit;
    val actualCommit2Msg: String = logIterator.next().getShortMessage();
    assertEquals(rewordedCommitMessage, actualCommit2Msg);

    val actualCommit3Msg: String = logIterator.next().getShortMessage();
    assertFalse(rewordedCommitMessage equals actualCommit3Msg);
  }

  @Test
  @Ignore
  def rewordFirstCommit {
    // arrange
    implicit val git = new Git(db);
    val rebaser: Rebaser = new Rebaser(git)

    val commit1Msg: String = "Add file1"
    val commit2Msg: String = "Add file2"
    val commit3Msg: String = "Add file3"
    val c1 = createAddAndCommitFile(GitFile("file1", "content for file1"), commit1Msg)
    val c2 = createAddAndCommitFile(GitFile("file2", "content for file2"), commit2Msg)
    val c3 = createAddAndCommitFile(GitFile("file3", "content for file3"), commit3Msg)

    // act
    val rewordedCommitMessage: String = "reworded commit message"
    val res = rebaser.rewordCommit(c1, rewordedCommitMessage)

    // assert
    //    assertEquals(Status.OK, res.getStatus());
    val logIterator: util.Iterator[RevCommit] = git.log().all().call().iterator();

    val array: Array[RevCommit] = asScalaIterator(logIterator).toArray


    // first message
    val actualCommit1Msg: String = array(0).getShortMessage();
    assertEquals(rewordedCommitMessage, actualCommit1Msg);

    // second commit
    val actualCommit2Msg: String = array(1).getShortMessage();
    assertFalse(rewordedCommitMessage equals actualCommit2Msg);

    // third commit
    val actualCommit3Msg: String = array(2).getShortMessage();
    assertFalse(rewordedCommitMessage equals actualCommit3Msg);
  }

  @Test
  def changeCommitOrder {
    // arrange
    implicit val git = new Git(db);
    val rebaser: Rebaser = new Rebaser(git)

    val commit1Msg: String = "Add file1"
    val commit2Msg: String = "Add file2"
    val commit3Msg: String = "Add file3"
    val c1 = createAddAndCommitFile(GitFile("file1", "content for file1"), commit1Msg)
    val c2 = createAddAndCommitFile(GitFile("file2", "content for file2"), commit2Msg)
    val c3 = createAddAndCommitFile(GitFile("file3", "content for file3"), commit3Msg)

    // act
    val res = rebaser.swapWithNextCommit(c2)

    // assert
    assertEquals(Status.OK, res.getStatus());
    val logIterator: util.Iterator[RevCommit] = git.log().all().call().iterator();

    val lastCommit: RevCommit = logIterator.next()
    assertEquals(commit2Msg, lastCommit.getShortMessage());

    val secondToLastCommit: RevCommit = logIterator.next()
    assertEquals(commit3Msg, secondToLastCommit.getShortMessage());
  }

  @Test
  def testRebaseInteractiveReword {
    // arrange
    implicit val git = new Git(db);
    val rebaser: Rebaser = new Rebaser(git)

    val c1 = createAddAndCommitFile(GitFile("file1", "content for file1"), "Add file1")
    val c2 = createAddAndCommitFile(GitFile("file2", "content for file2"), "Add file2")
    val c3 = createAddAndCommitFile(GitFile("file1", "updated content for file1"), "Updated file1 on master")
    val c4 = createAddAndCommitFile(GitFile("file2", "updated content for file2"), "updated file2 on side")

    // act
    val res: RebaseResult = git.rebase().setUpstream(c1).runInteractively(new RebaseCommand.InteractiveHandler {
      def prepareSteps(javaSteps: util.List[RebaseCommand.Step]) {
        val steps: mutable.Buffer[Step] = asScalaBuffer(javaSteps)
        steps foreach {
          step => {
            System.out.println(step)
            step.setAction(Action.REWORD)
          }
        }
      }

      def modifyCommitMessage(commit: String): String = {
        return "rewritten commit message"
      }
    }).call

    // assert
    //    assertEquals(Status.OK, res.getStatus());
    val logIterator: util.Iterator[RevCommit] = git.log().all().call().iterator();
    logIterator.next();
    // skip first commit;
    val actualCommitMag: String = logIterator.next().getShortMessage();
    assertEquals("rewritten commit message", actualCommitMag);
  }

  def createAddAndCommitFile(file: GitFile, commitMsg: CommitMessage)(implicit git: Git): RevCommit = {
    writeTrashFile(file.name, file.content);
    git.add().addFilepattern(file.name).call();
    git.commit().setMessage(commitMsg).call()
  }
}
