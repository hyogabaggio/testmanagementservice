package services.database

import models.Conversions.domainToAnyRef
import models.Domain
import net.liftweb.json._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import net.liftweb.json.Serialization.{read, write}

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

  /* Cette methode verifie d'abord que les valeurs respectent toutes les contraintes prescrites dans le modele avant
  d'essayer de saver
    */
  def checkAndSave(dataModel: AnyRef): Any = {
    // Validation du modele
    // La methode validate retourne une liste d'erreurs du modele
    // On ne save que si cette liste est vide
    dataModel.validate.isEmpty match {
      case true => {
        // les valeurs envoyées sont valides

        // on serialize le case class
        implicit val formats = Serialization.formats(NoTypeHints)
        val jsonDataModel = write(dataModel)

        //Save dans Redis
        //On recupere le type de datastructure
        val redisStructure = dataModel.asInstanceOf[Domain].getSet(dataModel) get ("redisStructure") //TODO Attention ceci implique qu'on ne peut save qu'un domain, i.e une classe qui extends Domain
        val redisService: RedisService = new RedisService()
        redisService.save(jsonDataModel, redisStructure.toString)

      }
      case _ => return dataModel.validate()
    }


  }
}
