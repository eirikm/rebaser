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

class ReorderCommitsTest extends AbstractRebaserTest {

  @Test
  def swapWithNext {
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
    val res = rebaser.swapWithNextCommit(c2).get

    // assert
    assertEquals(Status.OK, res.getStatus());
    val logIterator: util.Iterator[RevCommit] = git.log().all().call().iterator();

    val lastCommit: RevCommit = logIterator.next()
    assertEquals(commit2Msg, lastCommit.getShortMessage());

    val secondToLastCommit: RevCommit = logIterator.next()
    assertEquals(commit3Msg, secondToLastCommit.getShortMessage());
  }

  @Test
  def swapWithPrevious {
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
    val res = rebaser.swapWithPreviousCommit(c3).get

    // assert
    assertEquals(Status.OK, res.getStatus());
    val logIterator: util.Iterator[RevCommit] = git.log().all().call().iterator();

    val lastCommit: RevCommit = logIterator.next()
    assertEquals(commit2Msg, lastCommit.getShortMessage());

    val secondToLastCommit: RevCommit = logIterator.next()
    assertEquals(commit3Msg, secondToLastCommit.getShortMessage());
  }
}
