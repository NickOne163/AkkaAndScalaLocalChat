package localchat

import akka.actor.{Actor, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Put
import javafx.scene.Scene
import javafx.scene.control.TextField
import javafx.stage.Stage
import localchat.ChatActor.chatArea

object Sender {

  def props(from: String,to: String) = Props(new Sender(from,to))

}

case class OpenPrivateTextField()

class Sender(from: String,to: String) extends Actor {
  import akka.cluster.pubsub.DistributedPubSubMediator.Send

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Put(self)

  def receive = {
    case OpenPrivateTextField() =>
      val input = new TextField()
      input.setOnAction(_ => {
        val message = from + " : " + input.getText
        chatArea.appendText("Вы шепчете [" + to + "]: " + input.getText + "\n")
        val path = "/user/" + to  + "/" + to
        mediator ! Send(path, message,localAffinity = true)
        input.clear()
      })
      input.setPrefSize(400,30)
      val stage = new Stage()
      stage.setTitle("Приватное сообщение для [ " + to + " ]")
      stage.setScene(new Scene(input))
      stage.show()

  }
}
