package services.database.redis

import akka.actor.ActorSystem
import com.redis.RedisClient

/**
 * Created by hyoga on 07/12/2014.
 *
 * Classe qui centralise les methodes d'accés à la bdd Redis.
 */
trait RedisService {

  val redisClient = RedisClient("localhost", 6379) //TODO centraliser l'acces à la bdd dans un fichier de config

  /*
  Methode de save de toute entrée dans Redis.
  Des jsons sont savés.
  Le type de structure (dataStructure) est requis, afin de savoir dans quel type de table l'enregistrement aura lieu. Cette valeur est stockée dans chaque model, propriété "redisStructure".
  Le type de classe (className) est aussi requis, afin de gerer l'incrementation des ids. Redis ne les incremente pas automatiquement, so, ces ids sont gérés dans une table à part, de type String. Avant chaque save, une entrée du genre <<id:className>> est enregistrée, Ex: 2:Users, l'id etant incrementé. Cet id sera l'id du nouvel enregistrement à faire.
   */
  def save(dataMap: Map[String, Any], dataStructure: String, classId: String)(implicit system: ActorSystem): Any = {   //TODO ??????????


    //Save dans redis
    dataStructure match {
      case "hash" => {
        val redisHashService: RedisHashService = RedisHashService

      }
      case _ => None

    }

  }
}

/*
http://www.michaelpollmeier.com/2014/06/29/simple-dependency-injection-scala/
 */
object RedisService extends RedisService
