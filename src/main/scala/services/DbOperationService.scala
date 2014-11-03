package services

import com.redis.RedisClient
import net.liftweb.json._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Created by macbookpro on 19/10/2014.
 */
class DbOperationService {

 // val redis = new RedisClient("localhost", 6379)     //TODO centraliser l'acces à la bdd
  val fmt = DateTimeFormat.forPattern("yyMMddHHmmssSSS")


  def save(jsonValue: String, tableName: String): Boolean = {

    Console.println("DbOperationService save at = " + new DateTime().toString(fmt))
    val mapValue = parse(jsonValue).values.asInstanceOf[Map[String, Any]]
    Console.println("jsonValue= " + mapValue)
  //  redis.hmset(tableName, mapValue)
        true
  }
}
