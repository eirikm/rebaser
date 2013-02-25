package rebaser.gui

import swing._
import event.{Key, KeyPressed}
import scala.swing.BorderPanel.Position._

class RewordDialog(val inputText: String) extends Dialog {
  var rewordedCommitMessage: Option[String] = None

  val commitMessage = new TextArea(inputText) {
    border = Swing.EtchedBorder
    columns = 20
    rows = 5

    listenTo(keys)

    reactions += {
      case KeyPressed(_, Key.Escape, _, _) => cancelAction()
      case KeyPressed(_, Key.Enter, Key.Modifier.Meta, _) => okAction()
    }
  }

  title = "Reword commit"
  modal = true
  contents = new BorderPanel {
    layout(new BoxPanel(Orientation.Vertical) {
      border = Swing.EmptyBorder(5, 5, 5, 5)

      contents += new Label("Commit message:")
      contents += commitMessage
    }) = North

    val cancelButton: Button = new Button(Action("Cancel") {
      cancelAction()
    })
    val okButton: Button = new Button(Action("OK") {
      okAction()
    })
    layout(new FlowPanel(FlowPanel.Alignment.Right)(okButton, cancelButton)) = South
  }


  def okAction() {
    if (inputText != commitMessage.text) {
      rewordedCommitMessage = Some(commitMessage.text)
    }
    RewordDialog.this.dispose()
  }

  def cancelAction() {
    RewordDialog.this.dispose()
  }

  open()
}
