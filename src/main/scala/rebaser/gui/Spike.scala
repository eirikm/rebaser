package gui

import swing._
import event.{Key, KeyPressed}

object Spike extends SimpleSwingApplication {

  def top = new MainFrame {
    title = "Rebaser GUI (second draft)"
    location = new Point(10, 100)

    val commitList = List("one", "two", "three", "four")

    contents = new ListView(commitList) {
      listenTo(keys)

      reactions += {
        case KeyPressed(_, Key.Down, Key.Modifier.Control, _) => moveCommitDownCommand()
        case KeyPressed(_, Key.Up, Key.Modifier.Control, _) => moveCommitUpCommand()
        case KeyPressed(_, Key.R, 0, _) => rewordCommitCommand()
        case KeyPressed(_, Key.P, 0, _) => prependCommitsCommand()
        case KeyPressed(_, Key.X, 0, _) => explodeCommitCommand()
        case KeyPressed(_, Key.S, 0, _) => squashCommitsCommand()
        case KeyPressed(_, Key.F5, 0, _) => println(Key.F5 + ": Refresh")

        //        case KeyPressed(source, key, modifier, location) =>
        //          println("key: " + key + ", modifier: " + modifier)
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

      def rewordCommitCommand() {
        print(Key.R + ": ")
        selection.indices.size match {
          case 0 => println("Do nothing. No commit selected")
          case 1 => println("reword commit (" + selection.items.head + ")")
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
