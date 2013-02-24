package rebaser.gui

import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.lib.{ObjectId, Repository}
import java.io.File
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import java.lang.Iterable
import scala.collection.JavaConversions._

object Spike extends App {

  val builder: FileRepositoryBuilder = new FileRepositoryBuilder()

  val repository: Repository = builder.setGitDir(new File("/Users/eirikm/src/git/rebaser-test-repo/.git"))
    .readEnvironment() // scan environment GIT_* variables
    .findGitDir() // scan up the file system tree
    .build()

  val oldestCommit: ObjectId = repository.resolve("HEAD~6")
  val newestCommit: ObjectId = repository.resolve("HEAD")

  val git: Git = new Git(repository)
  val log: Iterable[RevCommit] = git.log().addRange(oldestCommit, newestCommit).call()

  iterableAsScalaIterable(log) foreach {
    c : RevCommit =>
      println(c.getShortMessage)
  }
}
