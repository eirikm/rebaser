package gui

import swing._
import event.{Key, KeyPressed}

object Spike extends SimpleSwingApplication {

  def top = new MainFrame {
    title = "Rebaser GUI (first draft)"
    location = new Point(10, 100)

    contents = new ListView(Seq("one", "two", "three", "four")) {
      listenTo(keys)

      reactions += {
        case KeyPressed(_, Key.Down, Key.Modifier.Control, _) =>
          print(Key.Control + " + " + Key.Down + ": ")
          selection.items match {
            case Seq(commit) => println("move commit down (" + commit + ")")
            case Seq(commit, _*) => println("Do nothing. More than one commit selected")
            case _ => println("Do nothing. No commit selected")
          }
        case KeyPressed(_, Key.Up, Key.Modifier.Control, _) =>
          print(Key.Control + " + " + Key.Up + ": ")
          selection.items match {
            case Seq(commit) => println("move commit up (" + commit + ")")
            case Seq(commit, _*) => println("Do nothing. More than one commit selected")
            case _ => println("Do nothing. No commit selected")
          }
        case KeyPressed(_, Key.R, 0, _) =>
          print(Key.R + ": ")
          selection.items match {
            case Seq(commit) => println("reword commit (" + commit + ")")
            case Seq(commit, _*) => println("Do nothing. More than one commit selected")
            case _ => println("Do nothing. No commit selected")
          }
        case KeyPressed(_, Key.P, 0, _) =>
          print(Key.P + ": ")
          selection.items match {
            case Seq(commit) => println("prepend commit messages (" + commit + ")")
            case Seq(commit, _*) => println("prepend commit messages (" + selection.items + ")")
            case _ => println("Do nothing. No commit selected")
          }
        case KeyPressed(_, Key.E, 0, _) =>
          print(Key.E + ": ")
          selection.items match {
            case Seq(commit) => println("explode commit (" + commit + ")")
            case Seq(commit, _*) => println("Do nothing. More than one commit selected")
            case _ => println("Do nothing. No commit selected")
          }
        case KeyPressed(_, Key.S, 0, _) =>
          print(Key.S + ": ")
          selection.items match {
            case Seq(commit, _*) => println("squash commits (" + selection.items + ")")
            case _ => println("Do nothing. Zero or one commit selected")
          }

//        case KeyPressed(source, key, modifier, location) =>
//          println("key: " + key + ", modifier: " + modifier)
      }
    }
  }
}
