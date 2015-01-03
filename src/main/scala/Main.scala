
import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

import routes.RoutingActor
import utilities.ActorSystemProvider

/**
 * Created by macbookpro on 23/08/2014.
 */
object Main extends App /*with ActorSystemProvider*/ {
  // we need an ActorSystem to host our application in
  //implicit val system = ActorSystem("bossSystem")
  val actorSystemProvider: ActorSystemProvider = ActorSystemProvider
  implicit val system = actorSystemProvider.system
  // create and start our service actor
  val service = system.actorOf(Props[RoutingActor], "testmanagementrouting-service")
  //
  //
  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ! Http.Bind(service, "localhost", port = 8080)
}
