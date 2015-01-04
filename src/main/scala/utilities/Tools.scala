package utilities

import com.typesafe.config.ConfigFactory
import models.Domain
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


  /*
  Methode permettant de faire un binding entre une map et un modele.
  Un modele est une case class contenue dans le package Models.
  La map contient un ensemble de paires. Chaque pair etant le nom de propriété en key et la valeur de la propriété en value.

  Ex: Model: userInstance:User(nom:String, prenom:String)
  Map: map:Map(nom -> "Baggio", prenom -> "Roby")
  bindingFromMap(map, userInstance)
    => userInstance.nom = Baggio,
    => userInstance.prenom = Roby
   */
  def binding(classInstance: AnyRef, params: Map[String, Any]): AnyRef = {
    params.map(kv => bindingFromPair(kv, classInstance))
    return classInstance
  }

  /*
  Même methode que binding.
  Sauf que le params qui vient contient une map "httpbody". Il faut d'abord l'extraire, puis le binder.
   */
  def extractHttpbodyAndBind(classInstance: AnyRef, params: Map[String, Any]): AnyRef = {
    var mapHttpbody = getType(params.get("httpbody")).asInstanceOf[Map[String, Any]]
    mapHttpbody.map(kv => bindingFromPair(kv, classInstance))
    return classInstance
  }


  /*
     Methode permettant de faire un binding entre une pair "key-value" et un modele.
     Un modele est une case class contenue dans le package Models.
     La pair contient le nom de propriété en key et la valeur de la propriété en value.

     Ex: Model: userInstance:User(nom:String, prenom:String)
     Pair: kv:Pair(nom, "Baggio")
     bindingFromPair(kv, userInstance)
       => userInstance.nom = Baggio
  */
  def bindingFromPair(kv: (String, Any), classInstance: AnyRef): AnyRef = {
    if (classInstance.isInstanceOf[Domain])
      classInstance.asInstanceOf[Domain].getSet(classInstance) set(kv._1.toString, kv._2)

    // ne marche que sur les string, et encore, pas sur les option[string]
    //TODO voir http://stackoverflow.com/questions/1589603/scala-set-a-field-value-reflectively-from-field-name
    return classInstance
  }

}
