import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.junit.Test


import scala.Predef.String


class ExplodeCommitTest extends AbstractRebaserTest {

  @Test
  def explodeSecondCommit {
    // arrange
    implicit val git = new Git(db);
    val rebaser: Rebaser = new Rebaser(git)

    val commit2Msg: String = "Add file2"
    val commit3Msg: String = "Add file3"
    val c1 = createNotRelevantInitialCommit()
    val c2 = createAddAndCommitFiles(
      List(
        GitFile("file1", "content for file1"),
        GitFile("file2", "content for file2")),
      commit2Msg)
    val c3 = createAddAndCommitFile(GitFile("file3", "content for file3"), commit3Msg)

    // act
    val newHead = rebaser.explodeCommit(c2)

    // assert
    //    assertEquals(Status.OK, res.getStatus());
//    val logIterator: util.Iterator[RevCommit] = git.log().all().call().iterator();
//    logIterator.next();
//    // skip first commit;
//    val actualCommit2Msg: String = logIterator.next().getShortMessage();
//    assertEquals(rewordedCommitMessage, actualCommit2Msg);
//
//    val actualCommit3Msg: String = logIterator.next().getShortMessage();
//    assertFalse(rewordedCommitMessage equals actualCommit3Msg);
  }

  def createAddAndCommitFiles(files: List[GitFile], commitMsg: CommitMessage)(implicit git: Git): RevCommit = {
    files foreach {
      file =>
        writeTrashFile(file.name, file.content);
        git.add().addFilepattern(file.name).call();
    }
    git.commit().setMessage(commitMsg).call()
  }
}
