package localchat

import akka.actor.ActorSystem
import akka.remote.RemoteTransportException
import com.typesafe.config.ConfigFactory
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.{Button, Label, TextField}
import javafx.scene.layout.GridPane
import javafx.stage.Stage

import java.net.BindException

object MyApp {

  def main(args: Array[String]): Unit = {
    Application.launch(classOf[MyApp])
  }

}

class MyApp extends Application{
  override def start(primaryStage: Stage): Unit  = {
    val grid = new GridPane()
    grid.setPrefSize(300,100)
    val btn = new Button("Войти в чат")
    val userName = new Label("Ваше имя: ")
    grid.add(userName,0,2)
    val input = new TextField()
    grid.add(input,1,2)
    val portLabel = new Label("Ваш порт: ")
    grid.add(portLabel,0,3)
    val inputPort = new TextField()
    btn.setOnMouseClicked(_ => {
      try {
      val user = input.getText
      input.clear()
      val config = ConfigFactory.parseString(
        s"""
           |akka.remote.artery.canonical.port = ${inputPort.getText}
           |""".stripMargin
      ).withFallback(ConfigFactory.load("application.conf"))
      val system = ActorSystem("ClusterSystem",config)
      val actorRef = system.actorOf(ChatActor.props(user).withDispatcher("javafx-dispatcher"),user)
        actorRef ! OpenGeneralChat()
      primaryStage.close()
        } catch {
          case  e: RemoteTransportException => {
            val exception = new Label("Порт уже используется")
            grid.add(exception, 1, 5)
          }


      }
    })
    grid.add(inputPort,1,3)
    grid.add(btn,3,2)
    primaryStage.setTitle("Вход в систему")
    primaryStage.setScene(new Scene(grid))
    primaryStage.show()
  }
}