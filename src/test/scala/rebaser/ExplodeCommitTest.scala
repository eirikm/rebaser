package rebaser

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.junit.Test

import scala.Predef.String

import org.junit.Assert._


class ExplodeCommitTest extends AbstractRebaserTest {

  @Test
  def explodeSecondCommitAdd {
    // arrange
    implicit val git = new Git(db);
    val rebaser: Rebaser = new Rebaser(git)

    val commit2Msg: CommitMessage = "Add file1 and file2"
    val commit3Msg: CommitMessage = "Add file3"

    val c1 = createNotRelevantInitialCommit()

    List(GitFile("file1"), GitFile("file2")).foreach {
      writeFile _ andThen gitAdd
    }
    val c2: RevCommit = gitCommit(commit2Msg)
    val c3 = createAddAndCommitFile(GitFile("file3"), commit3Msg)

    // act
    val newHead: RevCommit = rebaser.explodeCommit(c2).get

    // assert
    assertTrue(newHead != c3);

    val logIterator: java.util.Iterator[RevCommit] = git.log().all().call().iterator()

    val lastCommit: RevCommit = logIterator.next()
    assertEquals(c3.getFullMessage, lastCommit.getFullMessage)

    val splitCommit1: String = logIterator.next().getShortMessage()
    assertTrue(splitCommit1.contains(c2.getShortMessage))

    val splitCommit2: String = logIterator.next().getShortMessage()
    assertTrue(splitCommit2.contains(c2.getShortMessage))
  }

  def rndContent: String = {
    scala.util.Random.alphanumeric.take(20).mkString
  }

  def writeFile(file: GitFile): GitFile = {
    writeTrashFile(file.name, file.content)
    file
  }

  def gitAdd(file: GitFile)(implicit git: Git) = {
    git.add().addFilepattern(file.name).call()
  }

  def gitCommit(msg: CommitMessage)(implicit git: Git): RevCommit = {
    git.commit().setMessage(msg).call()
  }
}
