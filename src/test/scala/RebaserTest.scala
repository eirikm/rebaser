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

  @Test
  def initialCommit() {
    // arrange
    val git = new Git(db);
    writeTrashFile("a.txt", "content");
    git.add().addFilepattern("a.txt").call();
    val c: RevCommit = git.commit().setMessage("initial commit").call();

    // act
    val walk: TreeWalk = new TreeWalk(db);
    walk.addTree(new EmptyTreeIterator());
    walk.addTree(c.getTree());
    val result = DiffEntry.scan(walk);

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
    val git = new Git(db);
    writeTrashFile("a.txt", "content");
    git.add().addFilepattern("a.txt").call();
    val c1: RevCommit = git.commit().setMessage("commit a").call();
    writeTrashFile("b.txt", "content");
    git.add().addFilepattern("b.txt").call();
    val c2: RevCommit = git.commit().setMessage("commit b").call();

    // act
    val walk: TreeWalk = new TreeWalk(db);
    walk.addTree(c1.getTree);
    walk.addTree(c2.getTree());
    val result = DiffEntry.scan(walk);

    // assert
    assertThat(Integer.valueOf(result.size()), is(Integer.valueOf(1)));
    val entry: DiffEntry = result.get(0);
    assertThat(entry.getChangeType(), is(ChangeType.ADD));
    assertThat(entry.getNewPath(), is("b.txt"));
    assertThat(entry.getOldPath(), is(DEV_NULL));
  }
}
