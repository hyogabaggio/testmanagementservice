package services.database.redis

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import com.redis.RedisClient
import com.typesafe.config.ConfigFactory
import utilities.{Tools, ActorSystemProvider}
import scala.concurrent.duration._

import scala.concurrent.{Promise, Future}

/**
 * Created by hyoga on 07/12/2014.
 *
 * Classe qui centralise les methodes d'accés à la bdd Redis.
 */
trait RedisService {
  // Le client Redis utilise des actors pour la concurrence. De fait il a besoin de l'ActorSystem de l'application.
  // Il n'ya qu'un ActorSystem pour l'Application, instancié dans ActorSystemProvider. C'est un singleton, on l'injecte juste
  val actorSystemProvider: ActorSystemProvider = ActorSystemProvider
  implicit val system = actorSystemProvider.system
  implicit val executionContext = system.dispatcher
  // TODO new executionContext dédié à Redis, voir akka
  implicit val timeout = Timeout(2, TimeUnit.SECONDS)
  //implicit val timeout = 2.seconds

  //Connection à Redis
  val tools = new Tools
  val redisClient = new RedisClient(tools.getRedisUrl, tools.getRedisPort)

  /*
  Methode de save de toute entrée dans Redis.
  Des jsons sont savés.
  Le type de structure (dataStructure) est requis, afin de savoir dans quel type de table l'enregistrement aura lieu. Cette valeur est stockée dans chaque model, propriété "redisStructure".
  Le type de classe (className) est aussi requis, afin de gerer l'incrementation des ids. Redis ne les incremente pas automatiquement, so, ces ids sont gérés dans une table à part, de type String. Avant chaque save, une entrée du genre <<id:className>> est enregistrée, Ex: 2:Users, l'id etant incrementé. Cet id sera l'id du nouvel enregistrement à faire.
   */
  def save(dataMap: Map[String, String], dataStructure: String, classId: String): Any = {

    //Save dans redis
    dataStructure match {
      case "hash" => {
        val redisHashService: RedisHashService = RedisHashService
        return redisHashService.save(dataMap, classId)
      }

    }

  }

  /*
  Methode de recherche dans Redis via la key
   */
  def findByKey(dataStructure: String, classId: String, id: String): Any = {
    //recherche dans redis
    dataStructure match {
      case "hash" => {
        val redisHashService: RedisHashService = RedisHashService
        redisHashService.findByKey(classId, id)
      }
      case _ => None

    }
  }


  /*
  Methode de recherche dans Redis via la key
   */
  def find(dataStructure: String, params: Option[Map[String, Any]]): Any = {
    //recherche dans redis
    dataStructure match {
      case "hash" => {
        val redisHashService: RedisHashService = RedisHashService

        redisHashService.findByParams(params)
      }
      case _ => None

    }
  }
}

/*
http://www.michaelpollmeier.com/2014/06/29/simple-dependency-injection-scala/
 */
object RedisService extends RedisService
