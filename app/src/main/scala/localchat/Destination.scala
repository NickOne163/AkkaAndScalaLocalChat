package localchat

import akka.actor.Actor
import akka.cluster.pubsub.DistributedPubSub
import localchat.ChatActor.chatArea


class Destination extends Actor  {

  val mediator = DistributedPubSub(context.system).mediator

  def receive = {
    case msg: String =>
      val inputParams = msg.split(" ")
      val from = inputParams(0)
      chatArea.appendText("[ " + from + " ] шепчет вам : " + inputParams.drop(2).mkString(" ") + "\n")
  }
}