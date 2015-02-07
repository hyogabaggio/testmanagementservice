package services.database.redis


import utilities.Tools

import scala.concurrent.{Promise, Await, Future}
import scala.util.{Success, Failure}

/**
 * Created by hyoga on 14/12/2014.
 * Classe qui effectue les transactions vers la bdd Redis, mais ne gére que les tables de type Hash
 */
trait RedisHashService extends RedisService {

  /*
  Methode de save de json dans une Hash
  Afin de pouvoir les incrementer, les ids sont gérés dans une autre table,
  une table de type String. Lors de chaque save, on incremente cette table, et la valeur retournée sera l'id de la nouvelle entrée
   */
  def save(dataMap: Map[String, String], classId: String): Any = {
    // On incremente la table des ids afin de recuperer le nouvel id pour cet enregistrement
    var idValue = redisClient.incr(classId)
    Console.println("idValue = " + idValue)
    val key = determineId(classId, idValue)
    val dataMapToSave = setIdToDomain(dataMap, key)
    Console.println("dataMapToSave = " + dataMapToSave)
    return redisClient.hmset(key, dataMapToSave)
  }


  def findByKey(classType: String, id: String): Any = {
    val key = classType.toLowerCase + ':' + id
    Console.println("key = " + key)
    val rslt = redisClient.hgetall(key)
    Console.println("rslt = " + rslt)
    return rslt.get
  }


  /*
  Methode qui determine l'id du nouvel enregistrement.
  Sa structure est stockée au niveau du Domain (champ redisId)
  Ex: "users:id" pour la domain Users
  Lorsque cette structure contient 'id', le systeme remplace juste 'id' par sa valeur.
   Ex: "users:id" => "users:11".
   Si elle n'en contient pas, on lui rajoute juste la valeur.
   Ex: "users" => "users:11".
   */
  //TODO imposer la forme "users:id", une requette http ne contient pas de ':'
  def determineId(classId: String, idValue: Option[Long]): String = {
    var classIdCopy = classId
    if (classId.contains("id")) classIdCopy = classIdCopy.replace("id", idValue.get.toString) //TODO verifier dans le cas ou idValue ==None
    else {
      classIdCopy += ":" + idValue.get.toString //TODO verifier dans le cas ou idValue ==None
    }
    return classIdCopy
  }

  def setIdToDomain(dataMap: Map[String, String], classId: String): Map[String, String] = {
    var dataMapCopy = dataMap
    dataMapCopy += "id" -> classId
    return dataMapCopy
  }

}

/*
http://www.michaelpollmeier.com/2014/06/29/simple-dependency-injection-scala/
 */
object RedisHashService extends RedisHashService