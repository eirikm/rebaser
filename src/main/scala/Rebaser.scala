import java.io.File
import java.util
import org.eclipse.jgit.api.{LogCommand, Git}
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.{FileRepository, FileRepositoryBuilder}
import org.eclipse.jgit.treewalk.{EmptyTreeIterator, TreeWalk}
import scala.collection.JavaConverters._

class Rebaser(db: FileRepository) {
  def getAffectedFiles(commit: RevCommit): java.util.List[DiffEntry] = {
    val walk: TreeWalk = new TreeWalk(db);
    walk.addTree(new EmptyTreeIterator());
    walk.addTree(commit.getTree());
    DiffEntry.scan(walk)
  }

}

object Rebaser extends App {
}
