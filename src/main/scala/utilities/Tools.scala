package utilities

import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

/**
 * Created by hyoga on 24/11/2014.
 */
class Tools {


  //Methode qui prend en argument un Option[Any] et qui retourne Any
  def getType(element: Option[Any]): Any = {
    element match {
      case Some(x: Any) => x
      case _ => None
    }
  }


  // Check if a string is a number
  def isNumber(x: String): Boolean = {
    x.forall(_.isDigit)
  }


  //check if a string is a datetime
  def isDateTime(text: String): Boolean = {
    try {
      Console.println("datePattern = " + new Constantes {}.datePattern)
      val dtf = DateTimeFormat.forPattern(new Constantes {}.datePattern);
      dtf.parseDateTime(text);
      return true;
    } catch {
      //  case iae:IllegalArgumentException =>   return false;
      case iae: Exception => return false;
    }
  }


  def toDateTime(text: String): Option[DateTime] = {
    val formatter: DateTimeFormatter = DateTimeFormat.forPattern(new Constantes {}.datePattern)
    isDateTime(text) match {
      case true => Some(formatter.parseDateTime(text))
      case _ => None
    }

  }

  def getEnvironment(): String = {
    return "development" //TODO positionner une valeur quelque part pour renvoyer soit "development" soit "production"
  }


  def getRedisUrl(): String = {
    val databaseConfig = ConfigFactory.load().getConfig("database")
    val redisConfig = databaseConfig.getConfig("redis")
    val envConfig = redisConfig.getConfig(getEnvironment)
    return envConfig.getString("url")
  }


  def getRedisPort(): Int = {
    val databaseConfig = ConfigFactory.load().getConfig("database")
    val redisConfig = databaseConfig.getConfig("redis")
    val envConfig = redisConfig.getConfig(getEnvironment)
    return envConfig.getInt("port")
  }

}
