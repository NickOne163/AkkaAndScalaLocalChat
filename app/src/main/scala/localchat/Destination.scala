package localchat

import akka.actor.{Actor, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import localchat.MyApp.chatArea


class Destination extends Actor  {
  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  def receive: Receive = {
    case msg: String =>
      val inputParams = msg.split(" ")
      val from = inputParams(0)
      chatArea.appendText(s"[$from] шепчет вам : ${inputParams.drop(2).mkString(" ")} \n")
  }
}