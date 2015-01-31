package services.database

import akka.actor.Actor
import models.Conversions.domainToAnyRef
import models.Domain
import net.liftweb.json._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import net.liftweb.json.Serialization.{read, write}
import services.database.redis.RedisService
import utilities.Constantes

/**
 * Created by macbookpro on 19/10/2014.
 */
class DbOperationService extends Actor{

  val redisService: RedisService = RedisService
  val fmt = DateTimeFormat.forPattern("yyMMddHHmmssSSS")

  def receive = {
    case data => println("DbOperationService processing data")
  }


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
        if (redisId == null || redisId == "" || redisStructure == null || redisStructure == "") return "redisStructure.or.redisId.not.found"
        else if (!new Constantes {}.datastructures.contains(redisStructure)) return "redisStructure.not.valid"

       val saving = redisService.save(dataMap, redisStructure.toString, redisId.toString)
        Console.println("dboperation service saving= "+saving)
        /* idée: vu que le save se passe via un actor, donc avec un autre thread, so pourquoi ne pas, aprés la validation du modéle,
        juste renvoyer true, i.e considerer que dès que la validation est ok, le save se passera bien ? Gain en rapidité.
        Ajouter une sorte de Cron qui repassera sur les save qui ont echoué et fera something

        ERROR eue:
        [ERROR] [01/04/2015 15:51:35.964] [bossSystem-akka.actor.default-dispatcher-8] [ActorSystem(bossSystem)] Uncaught error from thread [bossSystem-akka.actor.default-dispatcher-8] (scala.runtime.NonLocalReturnControl$mcZ$sp)
         */
        //TODO considerer l'idée juste au dessus
        return true

      }
      case _ => return dataModel.validate()
    }


  }

  /*
  Methode qui recherche un enregistrement via l'id
   */
  def getById(dataModel: AnyRef, classType: String, id:String):Any = {
    val redisStructure = dataModel.asInstanceOf[Domain].getSet(dataModel) get ("redisStructure")
    // On s'assure que redisStructure n'est pas nul
    //Et que la dataStructure envoyée est contenue dans la liste des dataStructure acceptées par l'appli
    if (redisStructure == null || redisStructure == "") return "redisStructure.or.redisId.not.found"
    else if (!new Constantes {}.datastructures.contains(redisStructure)) return "redisStructure.not.valid"

    redisService.findByKey(redisStructure.toString, classType, id)
  }
}


/*
http://www.michaelpollmeier.com/2014/06/29/simple-dependency-injection-scala/
 */
//object DbOperationService extends DbOperationService
