import java.util
import org.eclipse.jgit.api.{MergeResult, Git}
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.{EmptyTreeIterator, TreeWalk}
import org.junit.Test


import org.hamcrest.CoreMatchers.is
import org.junit.Assert.assertThat
import org.eclipse.jgit.diff.DiffEntry.DEV_NULL

class RebaserTest extends RepositoryTestCase {

  case class File(name: String, content: String)
  type CommitMessage = String


  @Test
  def initialCommit() {
    // arrange
    implicit val git = new Git(db);
    val rebaser: Rebaser = new Rebaser(db)

    val commit: RevCommit = createAddAndCommitFile(File("a.txt", "content"), "initial commit")

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
  def normalCommit() {
    // arrange
    implicit val git = new Git(db);
    val rebaser: Rebaser = new Rebaser(db)

    createAddAndCommitFile(File("a.txt", "content"), "commit a")
    val commit = createAddAndCommitFile(File("b.txt", "content"), "commit b")

    // act
    val result: util.List[DiffEntry] = rebaser.getAffectedFiles(commit)

    // assert
    assertThat(Integer.valueOf(result.size()), is(Integer.valueOf(1)));
    val entry: DiffEntry = result.get(0);
    assertThat(entry.getChangeType(), is(ChangeType.ADD));
    assertThat(entry.getNewPath(), is("b.txt"));
    assertThat(entry.getOldPath(), is(DEV_NULL));
  }

  def createAddAndCommitFile(file: File, commitMsg: CommitMessage)(implicit git: Git): RevCommit = {
    writeTrashFile(file.name, file.content);
    git.add().addFilepattern(file.name).call();
    git.commit().setMessage(commitMsg).call()
  }
}
