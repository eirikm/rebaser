package gui

import swing._
import event.{Key, KeyPressed}
import rebaser.gui.RewordDialog
import rebaser.Rebaser
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.lib.{AbbreviatedObjectId, ObjectId, Repository}
import java.io.File
import org.eclipse.jgit.api.{RebaseResult, Git}
import java.lang.Iterable
import org.eclipse.jgit.revwalk.RevCommit
import collection.JavaConversions._
import swing.ListView.Renderer
import scala.swing.BorderPanel.Position._

object RebaserApp extends SimpleSwingApplication {
  val DEBUG = false
  val builder: FileRepositoryBuilder = new FileRepositoryBuilder()
  var repoPath: String = "/Users/eirikm/src/git/rebaser-test-repo/.git"

  var repository: Repository = _
  var git: Git = _
  var rebaser: Rebaser = _

  var oldestCommitParam: String = _
  var oldestCommit: ObjectId = _
  lazy val newestCommit: ObjectId = head()
  def log: Iterable[RevCommit] = git.log().addRange(oldestCommit, newestCommit).call()
  def commitList: List[RevCommit] = iterableAsScalaIterable(log).toList


  override def main(args: Array[String]) {
    if (!args.isEmpty) {
      repoPath = args(0)
      oldestCommitParam = args(1)
    } else {
      oldestCommitParam = "HEAD~6"
    }

    repository = builder.setGitDir(new File(repoPath))
      .readEnvironment() // scan environment GIT_* variables
      .findGitDir() // scan up the file system tree
      .build()
    oldestCommit = repository.resolve(oldestCommitParam)
    git = new Git(repository)
    rebaser = new Rebaser(git)
    super.main(args)
  }

  def head(): ObjectId = repository.resolve("HEAD")

  def log(oldestCommit: ObjectId, newestCommit: ObjectId): List[RevCommit] =
    iterableAsScalaIterable(git.log().addRange(oldestCommit, newestCommit).call()).toList

  def debug(message: => String) {
    if (DEBUG) println(message)
  }

  def top = new MainFrame {
    title = "Rebaser GUI (second draft)"
    location = new Point(10, 100)

    contents = new BorderPanel {
      layout(createInfoBar) = North
      layout(createCommitTable) = Center
      layout(createStatusBar) = South
    }


    def createCommitTable: ListView[RevCommit] = {
      new ListView(commitList) {
        renderer = Renderer(_.getShortMessage)
        listenTo(keys)

        reactions += {
          case KeyPressed(_, Key.R, 0, _) => rewordCommitCommand()
//          case KeyPressed(_, Key.Down, Key.Modifier.Control, _) => moveCommitDownCommand()
//          case KeyPressed(_, Key.Up, Key.Modifier.Control, _) => moveCommitUpCommand()
          case KeyPressed(_, Key.P, 0, _) => prependCommitsCommand()
          case KeyPressed(_, Key.X, 0, _) => explodeCommitCommand()
          case KeyPressed(_, Key.S, 0, _) => squashCommitsCommand()
          case KeyPressed(_, Key.F5, 0, _) => println(Key.F5 + ": Refresh")

          //        case KeyPressed(source, key, modifier, location) =>
          //          println("key: " + key + ", modifier: " + modifier)
        }

        def rewordCommitCommand() {
          val prefix: String = (Key.R + ": ")
          selection.indices.size match {
            case 0 => debug(prefix + "Do nothing. No commit selected")
            case 1 => {
              val selectedCommit: RevCommit = selection.items.head
              debug(prefix + "reword commit (" + selectedCommit.getId + ")")
              for (newCommitMessage <- new RewordDialog(selectedCommit.getFullMessage).rewordedCommitMessage) {
                val result: RebaseResult = rebaser.rewordCommit(selectedCommit, newCommitMessage)
                listData = log(oldestCommit, head())
              }
            }
            case _ => debug(prefix + "Do nothing. More than one commit selected")
          }
        }

        def moveCommitDownCommand() {
          val prefix = (Key.Control + " + " + Key.Down + ": ")
          selection.indices.size match {
            case 0 => println("Do nothing. No commit selected")
            case 1 =>
              val selectedIndex: Int = selection.indices.head
              if (selectedIndex == commitList.size - 1)
                debug(prefix + "can't move past last commit")
              else {
                debug(prefix + "move commit down (" + selection.items.head + ")")
                listData = swapWithNext(listData.toList, selectedIndex)
                selection.indices.empty
                selection.indices += selectedIndex + 1
              }
            case _ => debug(prefix + "Do nothing. More than one commit selected")
          }
        }

        def moveCommitUpCommand() {
          val prefix = (Key.Control + " + " + Key.Up + ": ")
          selection.indices.size match {
            case 0 => debug(prefix + "Do nothing. No commit selected")
            case 1 =>
              val selectedIndex: Int = selection.indices.head
              if (selectedIndex == 0)
                debug(prefix + "can't move before first commit")
              else {
                debug(prefix + "move commit up (" + selection.items.head + ")")
                listData = swapWithPrevious(listData.toList, selectedIndex)
                selection.indices.empty
                selection.indices += selectedIndex - 1
              }
            case _ => debug(prefix + "Do nothing. More than one commit selected")
          }
        }

        def prependCommitsCommand() {
          val prefix = (Key.P + ": ")
          selection.indices.size match {
            case 0 => debug(prefix + "Do nothing. No commit selected")
            case 1 => debug(prefix + "prepend commit messages (" + selection.items.head + ")")
            case _ => debug(prefix + "prepend commit messages (" + selection.items + ")")
          }
        }

        def explodeCommitCommand() {
          val prefix = (Key.X + ": ")
          selection.indices.size match {
            case 0 => debug(prefix + "Do nothing. No commit selected")
            case 1 => debug(prefix + "explode commit (" + selection.items.head + ")")
            case _ => debug(prefix + "Do nothing. More than one commit selected")
          }
        }

        def squashCommitsCommand() {
          val prefix = (Key.S + ": ")
          selection.indices.size match {
            case 0 => debug(prefix + "Do nothing. No commit selected")
            case 1 => debug(prefix + "Do nothing. Only one commit selected")
            case _ => debug(prefix + "squash commits (" + selection.items + ")")
          }
        }
      }
    }

    def swapWithNext[A](list: List[A], index: Int): List[A] =
      list match {
        case Nil => Nil
        case x1 :: x2 :: xs if index == 0 => x2 :: x1 :: xs
        case x :: xs => x :: swapWithNext(xs, index - 1)
      }

    def swapWithPrevious[A](list: List[A], index: Int) = swapWithNext(list, index - 1)
  }


  def createInfoBar: BoxPanel = {
    new BoxPanel(Orientation.Vertical) {
      contents += new Label(repoPath)
      val abbrObjId: AbbreviatedObjectId = oldestCommit.toObjectId.abbreviate(6)
      contents += new Label(abbrObjId.name() + " .. HEAD")
    }
  }

  def createStatusBar: FlowPanel = {
    new FlowPanel(FlowPanel.Alignment.Left)(new Label("status"))
  }
}