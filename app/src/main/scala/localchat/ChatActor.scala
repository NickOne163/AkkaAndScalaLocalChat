package localchat

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Put, Subscribe}
import javafx.application.Platform

case class PrintMessage(msg:String)

object ChatActor {
  def props(user: String): Props = Props(new ChatActor(user))
}

class ChatActor(var user: String) extends Actor{


  val destination: ActorRef = context.actorOf(Props[Destination], user)
  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  val cluster: Cluster = Cluster(context.system)

  cluster.subscribe(self, InitialStateAsEvents, classOf[MemberEvent])
  mediator ! Subscribe("general", self)
  mediator ! Put(destination)

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  override def receive: Receive = {
    case PrintMessage(msg:String) =>
      val inputParams = msg.split(" ")
      if(inputParams(2) == "/w") {
        val sender = context.actorOf(Sender.props(user,inputParams(3)))
        sender ! OpenPrivateTextField()
      } else {
        mediator ! Publish("general", msg + "\n")
      }
    case msg: String =>
      Platform.runLater( () => MyApp.chatArea.appendText(msg))
  }
}
