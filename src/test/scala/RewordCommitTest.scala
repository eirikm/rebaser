import java.util
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.junit.{Ignore, Test}


import org.junit.Assert._
import scala.Predef.String

import scala.collection.JavaConversions._

class RewordCommitTest extends AbstractRebaserTest {

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
}
