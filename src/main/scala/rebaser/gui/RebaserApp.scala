package gui

import swing._
import event.{Key, KeyPressed}
import rebaser.gui.RewordDialog
import rebaser.Rebaser
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.lib.{ObjectId, Repository}
import java.io.File
import org.eclipse.jgit.api.{RebaseResult, Git}
import java.lang.Iterable
import org.eclipse.jgit.revwalk.RevCommit
import collection.JavaConversions._
import swing.ListView.Renderer

object RebaserApp extends SimpleSwingApplication {
  val builder: FileRepositoryBuilder = new FileRepositoryBuilder()
  val repository: Repository = builder.setGitDir(new File("/Users/eirikm/src/git/rebaser-test-repo/.git"))
    .readEnvironment() // scan environment GIT_* variables
    .findGitDir() // scan up the file system tree
    .build()

  val git: Git = new Git(repository)

  val rebaser: Rebaser = new Rebaser(git)

  val oldestCommit: ObjectId = repository.resolve("HEAD~6")
  val newestCommit: ObjectId = head()
  val log: Iterable[RevCommit] = git.log().addRange(oldestCommit, newestCommit).call()
  val commitList: List[RevCommit] = iterableAsScalaIterable(log).toList

  def head(): ObjectId = repository.resolve("HEAD")

  def log(oldestCommit: ObjectId, newestCommit: ObjectId): List[RevCommit] =
    iterableAsScalaIterable(git.log().addRange(oldestCommit, newestCommit).call()).toList

  def top = new MainFrame {
    title = "Rebaser GUI (second draft)"
    location = new Point(10, 100)

    //val commitList = List("one", "two", "three", "four")

    contents = new ListView(commitList) {
      renderer = Renderer(_.getShortMessage)
      listenTo(keys)

      reactions += {
        case KeyPressed(_, Key.R, 0, _) => rewordCommitCommand()
        case KeyPressed(_, Key.Down, Key.Modifier.Control, _) => moveCommitDownCommand()
        case KeyPressed(_, Key.Up, Key.Modifier.Control, _) => moveCommitUpCommand()
        case KeyPressed(_, Key.P, 0, _) => prependCommitsCommand()
        case KeyPressed(_, Key.X, 0, _) => explodeCommitCommand()
        case KeyPressed(_, Key.S, 0, _) => squashCommitsCommand()
        case KeyPressed(_, Key.F5, 0, _) => println(Key.F5 + ": Refresh")

        //        case KeyPressed(source, key, modifier, location) =>
        //          println("key: " + key + ", modifier: " + modifier)
      }

      def rewordCommitCommand() {
        print(Key.R + ": ")
        selection.indices.size match {
          case 0 => println("Do nothing. No commit selected")
          case 1 => {
            val selectedCommit: RevCommit = selection.items.head
            println("reword commit (" + selectedCommit.getId + ")")
            for (newCommitMessage <- new RewordDialog(selectedCommit.getFullMessage).rewordedCommitMessage) {
              val result: RebaseResult = rebaser.rewordCommit(selectedCommit, newCommitMessage)
              listData = log(oldestCommit, head())
            }
          }
          case _ => println("Do nothing. More than one commit selected")
        }
      }

      def moveCommitDownCommand() {
        print(Key.Control + " + " + Key.Down + ": ")
        selection.indices.size match {
          case 0 => println("Do nothing. No commit selected")
          case 1 =>
            val selectedIndex: Int = selection.indices.head
            if (selectedIndex == commitList.size - 1)
              println("can't move past last commit")
            else {
              println("move commit down (" + selection.items.head + ")")
              listData = swapWithNext(listData.toList, selectedIndex)
              selection.indices.empty
              selection.indices += selectedIndex + 1
            }
          case _ => println("Do nothing. More than one commit selected")
        }
      }

      def moveCommitUpCommand() {
        print(Key.Control + " + " + Key.Up + ": ")
        selection.indices.size match {
          case 0 => println("Do nothing. No commit selected")
          case 1 =>
            val selectedIndex: Int = selection.indices.head
            if (selectedIndex == 0)
              println("can't move before first commit")
            else {
              println("move commit up (" + selection.items.head + ")")
              listData = swapWithPrevious(listData.toList, selectedIndex)
              selection.indices.empty
              selection.indices += selectedIndex - 1
            }
          case _ => println("Do nothing. More than one commit selected")
        }
      }

      def prependCommitsCommand() {
        print(Key.P + ": ")
        selection.indices.size match {
          case 0 => println("Do nothing. No commit selected")
          case 1 => println("prepend commit messages (" + selection.items.head + ")")
          case _ => println("prepend commit messages (" + selection.items + ")")
        }
      }

      def explodeCommitCommand() {
        print(Key.X + ": ")
        selection.indices.size match {
          case 0 => println("Do nothing. No commit selected")
          case 1 => println("explode commit (" + selection.items.head + ")")
          case _ => println("Do nothing. More than one commit selected")
        }
      }

      def squashCommitsCommand() {
        print(Key.S + ": ")
        selection.indices.size match {
          case 0 => println("Do nothing. No commit selected")
          case 1 => println("Do nothing. Only one commit selected")
          case _ => println("squash commits (" + selection.items + ")")
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
