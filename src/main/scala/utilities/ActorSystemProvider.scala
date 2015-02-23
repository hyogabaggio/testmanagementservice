package utilities

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import scalikejdbc.config.DBsWithEnv

/**
 * Created by hyoga on 01/01/2015.
 *
 * Classe d'initialisation de l'ActorSystem. Precision que l'ActorSystem doit être unique dans l'application.
 * Elle instancie aussi la connexion à la base de données, ainsi que les caracteristiques y afferant.
 * Cette classe est instanciée par le Main, lors du demarrage de l'application.
 */
trait ActorSystemProvider {

  // launch akka ActorSystem instance
  val akkaConfig = ConfigFactory.load().getConfig("akka")
  val actorSystemName = akkaConfig.getString("actor-system-name")
  Console.println("ActorSystemName = " + actorSystemName)
  implicit val system = ActorSystem(actorSystemName)

  // mysql plus utilisé
 /* //launch mysql instance
  // mysql data connexion are set in application.conf
  val tools = new Tools
  val environment = tools.getEnvironment()
  DBsWithEnv(environment).setupAll()      */
}


object ActorSystemProvider extends ActorSystemProvider