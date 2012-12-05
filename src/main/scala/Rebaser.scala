import java.io.File
import org.eclipse.jgit.api.{LogCommand, Git}
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.{FileRepository, FileRepositoryBuilder}
import scala.collection.JavaConverters._

object Rebaser extends App {
  val gitDir: File = new File("/Users/eirikm/src/git/rebaser/.git")

  val builder: FileRepositoryBuilder = new FileRepositoryBuilder()

  val repo: FileRepository =
    builder
      .setGitDir(gitDir)
      .readEnvironment()
      .findGitDir()
      .build()

  val git: Git = new Git(repo)

  val logs: Iterable[RevCommit] = git.log().call().asScala

  logs.foreach(rc => println(rc))
}
