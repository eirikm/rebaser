package gui

import swing._
import event.{Key, KeyPressed}

object Spike extends SimpleSwingApplication {

  def top = new MainFrame {
    title = "Rebaser GUI (first draft)"
    location = new Point(10, 100)

    val commitList: Seq[String] = Seq("one", "two", "three", "four")

    contents = new ListView(commitList) {
      listenTo(keys)

      reactions += {
        case KeyPressed(_, Key.Down, Key.Modifier.Control, _) =>
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
        case KeyPressed(_, Key.Up, Key.Modifier.Control, _) =>
          print(Key.Control + " + " + Key.Up + ": ")
          selection.indices.size match {
            case 0 => println("Do nothing. No commit selected")
            case 1 =>
              val selectedIndex: Int = selection.indices.head
              if (selectedIndex == 0)
                println("can't move before first commit")
              else
                println("move commit up (" + selection.items.head + ")")

            case _ => println("Do nothing. More than one commit selected")
          }
        case KeyPressed(_, Key.R, 0, _) =>
          print(Key.R + ": ")
          selection.indices.size match {
            case 0 => println("Do nothing. No commit selected")
            case 1 => println("reword commit (" + selection.items.head + ")")
            case _ => println("Do nothing. More than one commit selected")
          }
        case KeyPressed(_, Key.P, 0, _) =>
          print(Key.P + ": ")
          selection.indices.size match {
            case 0 => println("Do nothing. No commit selected")
            case 1 => println("prepend commit messages (" + selection.items.head + ")")
            case _ => println("prepend commit messages (" + selection.items + ")")
          }
        case KeyPressed(_, Key.X, 0, _) =>
          print(Key.X + ": ")
          selection.indices.size match {
            case 0 => println("Do nothing. No commit selected")
            case 1 => println("explode commit (" + selection.items.head + ")")
            case _ => println("Do nothing. More than one commit selected")
          }
        case KeyPressed(_, Key.S, 0, _) =>
          print(Key.S + ": ")
          selection.indices.size match {
            case 0 => println("Do nothing. No commit selected")
            case 1 => println("Do nothing. Only one commit selected")
            case _ => println("squash commits (" + selection.items + ")")
          }
        case KeyPressed(_, Key.F5, 0, _) =>
          println(Key.F5 + ": Refresh")

        //        case KeyPressed(source, key, modifier, location) =>
        //          println("key: " + key + ", modifier: " + modifier)
      }
    }
  }

  def swapWithNext[A](list: List[A], index: Int): List[A] =
    list match {
      case Nil => Nil
      case x1 :: x2 :: xs if index == 0 => x2 :: x1 :: xs
      case x :: xs => x :: swapWithNext(xs, index - 1)
    }
}
