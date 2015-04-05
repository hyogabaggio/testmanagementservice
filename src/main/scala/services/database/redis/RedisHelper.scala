package services.database.redis

import java.net.SocketException

import com.redis._
import com.redis.serialization.{Parse, Format}
import org.msgpack.ScalaMessagePack
import org.msgpack.`type`.Value

/**
 * Created by hyoga on 15/03/2015.
 */
class RedisHelper {

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
Ceci est en gros une simple reprise du driver scala-redis, afin d'enrichir certaines methodes et d'en rajouter d'autres.
https://github.com/debasishg/scala-redis
*/

class RedisClientImpl(redis: RedisClient) extends IO with Protocol {

  val host = redis.host
  val port = redis.port


  def cmd(command: String, args: Seq[Any]): /*Value*/ String = {
    val result = redis.send(command, args)(asByteArray)
    val deserialized = unpack(result)
   // Console.println("deserialized = " + deserialized)
  //  Console.println("deserialized = " + deserialized.toString())

    deserialized.toString()
  }


  def unpack(stream: Array[Byte]): Value = {
    // ScalaMessagePack.unpack[String](stream)
    ScalaMessagePack.readAsValue(stream)
  }

 //Version originale, plus sûre mais la flemme de gerer ces putains de Some et Option
  def asByteArrayElse[T](implicit parse: Parse[T]): Option[T] = redis.receive(redis.bulkReply orElse redis.multiBulkReply) match {
    case Some(bytes: Array[Byte]) => Some(parse(bytes))
    case _ => None
  }


  def asByteArray[T](implicit parse: Parse[T]): Array[Byte] = redis.receive(redis.integerReply orElse redis.singleLineReply orElse redis.bulkReply orElse redis.multiBulkReply) match {
    case Some(bytes: Array[Byte]) => bytes
    case _ => "0".getBytes("UTF-8")
  }
}


/*Tranforme un RedisClient en RedisClientImpl
  Et permet donc d'appliquer au RedisClient les methodes de RedisClientImpl
  Ex: RedisClientImpl a une methode validate(), avec cette conversion, on peut faire RedisClient.validate()
   => RedisClient a été Enrichi
  */
object RedisClientExtensions {
  implicit def redisClientImplConversion(redis: RedisClient) = new RedisClientImpl(redis)
}

