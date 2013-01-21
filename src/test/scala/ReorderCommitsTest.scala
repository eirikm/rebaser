import java.util
import org.eclipse.jgit.api.RebaseResult.Status
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.{RevWalk, RevCommit}
import org.junit.Test


import org.junit.Assert._
import scala.Predef.String

import scala.collection.JavaConversions._

class ReorderCommitsTest extends AbstractRebaserTest {

  @Test
  def swapWithNext {
    // arrange
    implicit val git = new Git(db);
    val rebaser: Rebaser = new Rebaser(git)

    val commit2Msg: String = "Add file2"
    val commit3Msg: String = "Add file3"
    val c1 = createNotRelevantInitialCommit()
    val c2 = createAddAndCommitFile(GitFile("file2", "content for file2"), commit2Msg)
    val c3 = createAddAndCommitFile(GitFile("file3", "content for file3"), commit3Msg)

    // act
    val newHead = rebaser.swapWithNextCommit(c2).get

    // assert
    assertTrue(newHead != c3);
    val logIterator: util.Iterator[RevCommit] = git.log().all().call().iterator();

    val lastCommit: RevCommit = logIterator.next()
    assertEquals(commit2Msg, lastCommit.getShortMessage());

    val secondToLastCommit: RevCommit = logIterator.next()
    assertEquals(commit3Msg, secondToLastCommit.getShortMessage());
  }

  @Test
  def swapWithNextWithConflicts {
    // arrange
    implicit val git = new Git(db);
    val rebaser: Rebaser = new Rebaser(git)

    val commit2Msg: String = "Add file2"
    val commit3Msg: String = "Add file3"
    val c1 = createNotRelevantInitialCommit()
    val c2 = createAddAndCommitFile(GitFile("file1", "content for file1"), commit2Msg)
    val c3 = createAddAndCommitFile(GitFile("file1", "content for file3"), commit3Msg)

    // act
    val newHead = rebaser.swapWithNextCommit(c2)

    // assert
    assertEquals(None, newHead)
    assertEquals(c3, db.resolve("HEAD"))
  }

  @Test
  def swapWithPrevious {
    // arrange
    implicit val git = new Git(db);
    val rebaser: Rebaser = new Rebaser(git)

    val commit2Msg: String = "Add file2"
    val commit3Msg: String = "Add file3"
    val c1 = createNotRelevantInitialCommit()
    val c2 = createAddAndCommitFile(GitFile("file2", "content for file2"), commit2Msg)
    val c3 = createAddAndCommitFile(GitFile("file3", "content for file3"), commit3Msg)

    // act
    val newHead = rebaser.swapWithPreviousCommit(c3).get

    // assert
    assertFalse(newHead == c3);
    val logIterator: util.Iterator[RevCommit] = git.log().all().call().iterator();

    val lastCommit: RevCommit = logIterator.next()
    assertEquals(commit2Msg, lastCommit.getShortMessage());

    val secondToLastCommit: RevCommit = logIterator.next()
    assertEquals(commit3Msg, secondToLastCommit.getShortMessage());
  }

  @Test
  def swapWithPreviousWithConflicts {
    // arrange
    implicit val git = new Git(db);
    val rebaser: Rebaser = new Rebaser(git)

    val commit2Msg: String = "Add file2"
    val commit3Msg: String = "Add file3"
    val c1 = createNotRelevantInitialCommit()
    val c2 = createAddAndCommitFile(GitFile("file1", "content for file1"), commit2Msg)
    val c3 = createAddAndCommitFile(GitFile("file1", "content for file3"), commit3Msg)

    // act
    val newHead = rebaser.swapWithPreviousCommit(c3)

    // assert
    assertEquals(None, newHead)
    assertEquals(c3, db.resolve("HEAD"))
  }
}
