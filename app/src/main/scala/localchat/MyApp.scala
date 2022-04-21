package localchat

import akka.actor.{ActorRef, ActorSystem}
import akka.remote.RemoteTransportException
import com.typesafe.config.ConfigFactory
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.{Button, Label, TextArea, TextField}
import javafx.scene.layout.{GridPane, VBox}
import javafx.scene.text.Font
import javafx.stage.Stage

class MyApp extends Application {
  override def start(primaryStage: Stage): Unit = {
    MyApp.showLoginWindow()
  }
}

object MyApp {

  val chatArea = new TextArea()
  chatArea.setFont(Font.font(36))
  chatArea.setPrefHeight(550)
  chatArea.setEditable(false)
  chatArea.setStyle("-fx-font-size: 1em;")

  def main(args: Array[String]): Unit = {
    Application.launch(classOf[MyApp])
  }

  def showLoginWindow(): Unit =  {
    val loginWindow = new Stage()
    val grid = new GridPane()
    val launchButton = new Button("Войти в чат")
    val usernameLabel = new Label("Ваше имя: ")
    val usernameInput = new TextField()
    val portLabel = new Label("Ваш порт: ")
    val portInput = new TextField()
    grid.setPrefSize(300,100)
    grid.add(usernameLabel,0,2)
    grid.add(usernameInput,1,2)
    grid.add(portLabel,0,3)
    grid.add(portInput,1,3)
    grid.add(launchButton,3,2)
    launchButton.setOnMouseClicked(_ => {
      try {
        val user = usernameInput.getText
        val config = ConfigFactory.parseString(
          s"""
             |akka.remote.artery.canonical.port = ${portInput.getText}
             |""".stripMargin
        ).withFallback(ConfigFactory.load("application.conf"))
        val system = ActorSystem("ClusterSystem",config)
        val userActor = system.actorOf(ChatActor.props(user),user)
        showGeneralChatWindow(user, userActor)
        loginWindow.close()
      } catch {
        case  _: RemoteTransportException =>
          portInput.clear()
          val exception = new Label("Порт уже используется")
          grid.add(exception, 1, 5)
      }
    })
    loginWindow.setTitle("Вход в систему")
    loginWindow.setScene(new Scene(grid))
    loginWindow.show()
  }

  def showGeneralChatWindow(user:String, actorRef: ActorRef): Unit = {
    val chatInputField = new TextField()
    chatInputField.setOnAction(_ => {
      val message = s"$user : ${chatInputField.getText}"
      chatInputField.clear()
      actorRef ! PrintMessage(message)
    })
    val root = new VBox(10,chatArea,chatInputField)
    root.setPrefSize(600, 600)
    val stage = new Stage()
    stage.setOnCloseRequest( _ => {
      System.exit(0)
    })
    stage.setTitle(s"Локальный чат. Ваше имя : $user ")
    stage.setScene(new Scene(root))
    stage.show()
  }



}
