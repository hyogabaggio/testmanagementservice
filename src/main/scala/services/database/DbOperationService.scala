package services.database

import akka.actor.Actor
import models.Conversions.domainToAnyRef
import models.Domain
import net.liftweb.json._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import net.liftweb.json.Serialization.{read, write}
import services.database.redis.RedisService
import utilities.{Validation, Constantes}

/**
 * Created by macbookpro on 19/10/2014.
 */
class DbOperationService {

  val redisService: RedisService = RedisService
  val fmt = DateTimeFormat.forPattern("yyMMddHHmmssSSS")


  /* Cette methode verifie d'abord que les valeurs respectent toutes les contraintes prescrites dans le modele avant
  d'essayer de saver
    */
  def checkAndSave(dataModel: AnyRef): Any = {
    /* Validation du modele
     La methode validate retourne une liste d'erreurs du modele
     On ne save que si cette liste est vide
    */
    dataModel.validate.isEmpty match {
      case true => {
        // les valeurs envoyées sont bien valides, donc on transforme le Domain en Map avant de le save()
        // On le transforme en Map car la methode de save de Redis ne prend en argument que des Iterable

        val dataMap = dataModel.toMap()
        //  Console.println("dataMap tostring= "+dataMap)

        //Save dans Redis
        //On recupere le type de datastructure et l'id
        val redisStructure = dataModel.asInstanceOf[Domain].getSet(dataModel) get ("redisStructure")
        val redisId = dataModel.asInstanceOf[Domain].getSet(dataModel) get ("redisId")
        // TODO ne pas systematiser l'utilisation de redis, peut-etre gerer un choix de bdd

        // On s'assure que redisStructure et redisId ne sont pas nuls
        //Et que la dataStructure envoyée est contenue dans la liste des dataStructure acceptées par l'appli
        if (redisId == null || redisId == "" || redisStructure == null || redisStructure == "") return new Validation(Map("Domain" -> "redisStructure.or.redisId.not.found"))
        else if (!new Constantes {}.datastructures.contains(redisStructure)) return new Validation(Map("Domain" -> "redisStructure.not.valid"))

        return redisService.save(dataMap, redisStructure.toString, redisId.toString)
      }
      case _ => return dataModel.validate.get
    }


  }

  /*
  Methode qui recherche un enregistrement via l'id
   */
  def getById(dataModel: AnyRef, classType: String, id: String): Any = {
    val redisStructure = dataModel.asInstanceOf[Domain].getSet(dataModel) get ("redisStructure")
    // On s'assure que redisStructure n'est pas nul
    //Et que la dataStructure envoyée est contenue dans la liste des dataStructure acceptées par l'appli
    if (redisStructure == null || redisStructure == "") return new Validation(Map("Domain" -> "redisStructure.or.redisId.not.found"))
    else if (!new Constantes {}.datastructures.contains(redisStructure)) return new Validation(Map("Domain" -> "redisStructure.not.valid"))

    redisService.findByKey(redisStructure.toString, classType, id)
  }
}


/*
http://www.michaelpollmeier.com/2014/06/29/simple-dependency-injection-scala/
 */
//object DbOperationService extends DbOperationService
