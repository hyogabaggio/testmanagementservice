package utilities

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat


/**
 * Created by hyoga on 06/12/2014.
 *
 * Classe qui permet d'ajouter des methodes à la classe String.
 * Ex: On implemente la methode toDateTime qui transforme un string en dateTime
 * Puis au lieu de faire toDateTime(string), on fait à la place string.toDateTime: plus cool
 */
class StringComplements(text: String) {

  // Methode qui transforme un string en Datetime.
  // A n'utiliser que si on est absolument sûr que la transformation est possible.
  // Sinon, preferer l'autre methode, toDateTimeOption, qui renvoie un Option[DateTime] (qui gére donc le None)
  def toDateTime(): DateTime = {
    val tools = new Tools
    val dateTime = tools.toDateTime(text)
    return tools.getType(dateTime).asInstanceOf[DateTime] // TODO verifier que si getType retourne None, ça ne plantera pas

  }

  def toDateTimeOption(): Option[DateTime] = {
    val tools = new Tools
    return tools.toDateTime(text)
  }


  // Check if a string is a number
  def isNumber(): Boolean = {
    text.forall(_.isDigit)
  }


  //check if a string is a datetime
  def isDateTime(): Boolean = {
    try {
      //  Console.println("datePattern = " + new Constantes {}.datePattern)
      val dtf = DateTimeFormat.forPattern(new Constantes {}.datePattern);
      dtf.parseDateTime(text);
      return true;
    } catch {
      //  case iae:IllegalArgumentException =>   return false;
      case iae: Exception => return false;
    }
  }

  // Methode qui verifie qi un string est un boolean
  def isBoolean(): Boolean = {
    text match {
      case "true" | "false" => return true
      case _ => return false
    }
  }

}

object Conversions {

  // Transforme un string en StringComplements.
  // Et permet donc d'appliquer au string les methodes de StringComplements
  // Ex: StringComplements a une methode isBoolean, avec cette conversion, on peut faire string.isBoolean
  implicit def stringToStringComplements(text: String) = new StringComplements(text)

}



