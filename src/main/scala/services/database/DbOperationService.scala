package services.database

import akka.actor.Actor
import models.Conversions.domainToAnyRef
import models.Domain
import net.liftweb.json._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import net.liftweb.json.Serialization.{read, write}
import services.database.redis.RedisService
import utilities.{Tools, Validation, Constantes}

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

        var dataMap = dataModel.toMap()

        //on ajoute la date de creation
        dataMap += "createdAt" -> DateTime.now().toString(DateTimeFormat.forPattern(new Constantes {}.datePattern))

        //Save dans Redis
        //On recupere le type de datastructure et l'id
        var redisStructure = dataModel.asInstanceOf[Domain].getSet(dataModel) get ("redisStructure")
        var redisId = dataModel.asInstanceOf[Domain].getSet(dataModel) get ("redisId")

        // TODO ne pas systematiser l'utilisation de redis, peut-etre gerer un choix de bdd

        // On s'assure que redisStructure et redisId ne sont pas nuls, hash etant la structure par defaut
        //Et que la dataStructure envoyée est contenue dans la liste des dataStructure acceptées par l'appli
        if (redisStructure.isInstanceOf[String] == false) redisStructure = "hash"
        if (!new Constantes {}.datastructures.contains(redisStructure)) return new Validation(Map("Domain" -> "redisStructure.not.valid"))
        if (redisId.isInstanceOf[String] == false) redisId = dataModel.getClass.getSimpleName.toLowerCase + ":id"

        //On recupere le type du model devant être enregistré: 'users' par exemple pour le model Users
        val classtype = dataModel.getClass.getSimpleName.toLowerCase

        return redisService.save(dataMap, redisStructure.toString, classtype, redisId.toString)
        //TODO juste avant le return, ne pas oublier de lancer un autre actor pour l'arangodb
        //TODO save dans arangodb: ne pas oublier de saver dans un noeud Context, qui contiendra tout ce qu'il ya dans le httpbody, sauf ce qu'il ya deja dans le case class
      }
      case _ => return dataModel.validate.get
    }
  }


  def checkAndUpdate(dataModel: AnyRef): Any = {
    /* Validation du modele
     La methode validate retourne une liste d'erreurs du modele
     On ne save que si cette liste est vide
    */
    dataModel.validate.isEmpty match {
      case true => {
        // les valeurs envoyées sont bien valides, donc on transforme le Domain en Map avant de le save()
        // On le transforme en Map car la methode de save de Redis ne prend en argument que des Iterable

        var dataMap = dataModel.toMap()

        //on ajoute la date de creation
        dataMap += "updatedAt" -> DateTime.now().toString(DateTimeFormat.forPattern(new Constantes {}.datePattern))

        //Save dans Redis
        //On recupere le type de datastructure et l'id
        var redisStructure = dataModel.asInstanceOf[Domain].getSet(dataModel) get ("redisStructure")
        var redisId = dataModel.asInstanceOf[Domain].getSet(dataModel) get ("redisId")

        // TODO ne pas systematiser l'utilisation de redis, peut-etre gerer un choix de bdd

        // On s'assure que redisStructure et redisId ne sont pas nuls, hash etant la structure par defaut
        //Et que la dataStructure envoyée est contenue dans la liste des dataStructure acceptées par l'appli
        if (redisStructure.isInstanceOf[String] == false) redisStructure = "hash"
        if (!new Constantes {}.datastructures.contains(redisStructure)) return new Validation(Map("Domain" -> "redisStructure.not.valid"))
        if (redisId.isInstanceOf[String] == false) redisId = dataModel.getClass.getSimpleName.toLowerCase + ":id"

        //On recupere le type du model devant être enregistré: 'users' par exemple pour le model Users
        val classtype = dataModel.getClass.getSimpleName.toLowerCase

        return redisService.update(dataMap, redisStructure.toString, classtype, redisId.toString)
        //TODO juste avant le return, ne pas oublier de lancer un autre actor pour l'arangodb
        //TODO save dans arangodb: ne pas oublier de saver dans un noeud Context, qui contiendra tout ce qu'il ya dans le httpbody, sauf ce qu'il ya deja dans le case class
      }
      case _ => return dataModel.validate.get
    }
  }

  /*
  Methode qui recherche un enregistrement via l'id
  Utilisée par le show dans le controller
   */
  def getById(dataModel: AnyRef, id: String): Any = {
    var redisStructure = dataModel.asInstanceOf[Domain].getSet(dataModel) get ("redisStructure")
    // On s'assure que redisStructure n'est pas nul
    //Et que la dataStructure envoyée est contenue dans la liste des dataStructure acceptées par l'appli
    if (redisStructure.isInstanceOf[String] == false) redisStructure = "hash"
    else if (!new Constantes {}.datastructures.contains(redisStructure)) return new Validation(Map("Domain" -> "redisStructure.not.valid"))

    //on recupere aussi la structure de l'id. Par defaut elle est de la forme "users:id" pour la class Users par exemple
    var redisId = dataModel.asInstanceOf[Domain].getSet(dataModel) get ("redisId")
    if (redisId.isInstanceOf[String] == false) redisId = dataModel.getClass.getSimpleName.toLowerCase + ":id"

    redisService.findByKey(redisStructure.toString, redisId.toString, id)
  }


  /*
 Methode qui recherche un enregistrement dans un model (dataModel) via des parametres
  */
  def get(dataModel: AnyRef, params: Option[Map[String, Any]]): Any = {
    var redisStructure = dataModel.asInstanceOf[Domain].getSet(dataModel) get ("redisStructure")
    // On s'assure que redisStructure n'est pas nul
    //Et que la dataStructure envoyée est contenue dans la liste des dataStructure acceptées par l'appli
    if (redisStructure.isInstanceOf[String] == false) redisStructure = "hash"
    else if (!new Constantes {}.datastructures.contains(redisStructure)) return new Validation(Map("Domain" -> "redisStructure.not.valid"))

    /* Meme si le model reçu (dataModel) est vide, on le transforme en Map et on l'envoie en params à la methode de suppression.
   On devra supprimer les index aussi, et comme ils sont set en dur dans le model, on les a automatiquement.
   On pourrait les recup ici directement et les envoyer en params au lieu d'envoyer le model en entier, mais on choisit juste de faire pareil que dans les autres méthodes. Afin de faire plus de copier/coller ;-P
   */
    val dataMap = dataModel.toMap()
    val classtype = dataModel.getClass.getSimpleName.toLowerCase
    redisService.find(dataMap, classtype, redisStructure.toString, params)
  }


  def delete(dataModel: AnyRef, id: String): Any = {
    var redisStructure = dataModel.asInstanceOf[Domain].getSet(dataModel) get ("redisStructure")
    // On s'assure que redisStructure n'est pas nul
    //Et que la dataStructure envoyée est contenue dans la liste des dataStructure acceptées par l'appli
    if (redisStructure.isInstanceOf[String] == false) redisStructure = "hash"
    else if (!new Constantes {}.datastructures.contains(redisStructure)) return new Validation(Map("Domain" -> "redisStructure.not.valid"))

    //on recupere aussi la structure de l'id. Par defaut elle est de la forme "users:id" pour la class Users par exemple
    var redisId = dataModel.asInstanceOf[Domain].getSet(dataModel) get ("redisId")
    if (redisId.isInstanceOf[String] == false) redisId = dataModel.getClass.getSimpleName.toLowerCase + ":id"

    //On recupere le type du model devant être enregistré: 'users' par exemple pour le model Users
    val classtype = dataModel.getClass.getSimpleName.toLowerCase


    /* Meme si le model reçu (dataModel) est vide, on le transforme en Map et on l'envoie en params à la methode de suppression.
    On devra supprimer les index aussi, et comme ils sont set en dur dans le model, on les a automatiquement.
    On pourrait les recup ici directement et les envoyer en params au lieu d'envoyer le model en entier, mais on choisit juste de faire pareil que dans les autres méthodes. Afin de faire plus de copier/coller ;-P
    */
    var dataMap = dataModel.toMap()

    //on ajoute la date de creation
    dataMap += "deletedAt" -> DateTime.now().toString(DateTimeFormat.forPattern(new Constantes {}.datePattern))

    redisService.delete(dataMap, redisStructure.toString, classtype, redisId.toString, id)
  }

}


/*
http://www.michaelpollmeier.com/2014/06/29/simple-dependency-injection-scala/
 */
//object DbOperationService extends DbOperationService
