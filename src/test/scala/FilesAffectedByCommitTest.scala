import java.util
import org.eclipse.jgit.api.RebaseResult.Status
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry._
import org.eclipse.jgit.revwalk.RevCommit
import org.hamcrest.CoreMatchers._
import org.junit.Test


import org.junit.Assert._
import scala.Predef.String


class FilesAffectedByCommitTest extends AbstractRebaserTest {

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
}
