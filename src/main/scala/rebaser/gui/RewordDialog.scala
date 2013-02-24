package rebaser.gui

import swing._
import scala.swing.BorderPanel.Position._

class RewordDialog(val inputText: String) extends Dialog {
  val commitMessage = new TextArea(inputText) {
    border = Swing.EtchedBorder
    columns = 20
    rows = 5
  }
  var rewordedCommitMessage: Option[String] = None

  title = "Reword commit"
  modal = true

  contents = new BorderPanel {
    layout(new BoxPanel(Orientation.Vertical) {
      border = Swing.EmptyBorder(5, 5, 5, 5)

      contents += new Label("Commit message:")
      contents += commitMessage
    }) = North

    val cancelButton: Button = new Button(Action("Cancel") {
      RewordDialog.this.dispose()
    })
    val okButton: Button = new Button(Action("OK") {
      if (inputText != commitMessage.text) {
        rewordedCommitMessage = Some(commitMessage.text)
      }
      RewordDialog.this.dispose()
    })
    layout(new FlowPanel(FlowPanel.Alignment.Right)(okButton, cancelButton)) = South
  }

  open()
}
