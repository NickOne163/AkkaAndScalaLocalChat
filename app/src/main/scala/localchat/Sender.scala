package localchat

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Put
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.TextField
import javafx.stage.Stage
import localchat.MyApp.chatArea

case class OpenPrivateTextField()

object Sender {
  def props(from: String,to: String): Props = Props(new Sender(from,to))
}

class Sender(from: String,to: String) extends Actor {
  import akka.cluster.pubsub.DistributedPubSubMediator.Send

  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  mediator ! Put(self)

  def receive: Receive = {
    case OpenPrivateTextField() =>
      val privateInput = new TextField()
      privateInput.setOnAction(_ => {
        val message = s"$from : ${privateInput.getText}"
        chatArea.appendText(s"Вы шепчете [$to]: ${privateInput.getText}\n")
        val path = s"/user/$to/$to"
        mediator ! Send(path, message,localAffinity = true)
        privateInput.clear()
      })
      privateInput.setPrefSize(400,30)
     Platform.runLater( () => {
       val stage = new Stage()
       stage.setTitle(s"Приватное сообщение для [$to]")
       stage.setScene(new Scene(privateInput))
       stage.show()
     })

  }
}
