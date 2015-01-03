package utilities

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

/**
 * Created by hyoga on 01/01/2015.
 */
trait ActorSystemProvider {
  // implicit def system: ActorSystem
  val akkaConfig = ConfigFactory.load().getConfig("akka")
  val actorSystemName = akkaConfig.getString("actor-system-name")
  Console.println("ActorSystemName = " + actorSystemName)
  implicit val system = ActorSystem(actorSystemName)
}


object ActorSystemProvider extends ActorSystemProvider