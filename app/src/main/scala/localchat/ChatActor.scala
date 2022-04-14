package localchat

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Put, Subscribe}
import javafx.scene.Scene
import javafx.scene.control.{TextArea, TextField}
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.Stage
import localchat.ChatActor.chatArea

object ChatActor {

  val chatArea = new TextArea()
  chatArea.setFont(Font.font(36))
  chatArea.setPrefHeight(550)
  chatArea.setEditable(false)
  chatArea.setStyle("-fx-font-size: 1em;")

  def props(user: String) = Props(new ChatActor(user))

}

case class OpenGeneralChat()

class ChatActor(var user: String) extends Actor{

  val destination: ActorRef = context.actorOf(Props[Destination].withDispatcher("javafx-dispatcher"), user)

  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  mediator ! Subscribe("general", self)
  mediator ! Put(destination)

  val cluster = Cluster(context.system)


  override def preStart() {
    cluster.subscribe(self, InitialStateAsEvents, classOf[MemberEvent])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)

  }


  override def receive: Receive = {
    case OpenGeneralChat() =>
      val input = new TextField()
      input.setOnAction(_ => {
        val message = user + " : " + input.getText
        val inputParams = message.split(" ")
        input.clear()
        if(inputParams(2) == "/w") {
          val sender = context.actorOf(Sender.props(user,inputParams(3)).withDispatcher("javafx-dispatcher"))
          sender ! OpenPrivateTextField()
        } else {
          mediator ! Publish("general", message)
        }
      })
      val root = new VBox(10,chatArea,input)
      root.setPrefSize(600, 600)
      val stage = new Stage()
      stage.setOnCloseRequest( _ => {
        context.stop(self)
        System.exit(0)
      })
      stage.setTitle("Локальный чат. Ваше имя : " + user)
      stage.setScene(new Scene(root))
      stage.show()
    case msg: String =>
        chatArea.appendText(msg + "\n")
  }
}
