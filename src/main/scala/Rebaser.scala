import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.revwalk.{RevWalk, RevCommit}
import org.eclipse.jgit.storage.file.FileRepository
import org.eclipse.jgit.treewalk.{EmptyTreeIterator, TreeWalk}

class Rebaser(db: FileRepository) {
  def getAffectedFiles(commit: RevCommit): java.util.List[DiffEntry] = {
    val walk: TreeWalk = new TreeWalk(db);

    commit.getParentCount match {
      case 0 => walk.addTree(new EmptyTreeIterator())
      case 1 => {
        val rw = new RevWalk(db)
        val parseCommit: RevCommit = rw.parseCommit(commit.getParent(0).getId)

        walk.addTree(parseCommit.getTree)
      }
      case _ => println("This should not happen")
    }
    walk.addTree(commit.getTree())
    DiffEntry.scan(walk)
  }

}

object Rebaser extends App {
}
