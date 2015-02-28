package services.database.redis


import utilities.Tools

import scala.concurrent.{Future}
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
    val idValue = redisClient.incr(classId)
    Console.println("idValue = " + idValue)
    val key = determineId(classId, idValue)
    val dataMapToSave = setIdToDomain(dataMap, key)
    // Console.println("dataMapToSave = " + dataMapToSave)
    val saving = redisClient.hmset(key, dataMapToSave)

    // After avoir enregistré dans la table principale, on lance l'enregistrement des indexes dans un Future (un autre thread, en gros, pour que cela ne bloque pas le flux principal)
    Future {
      val indexesList = dataMapToSave.get("index")
      if (indexesList != None && indexesList.get != None) {
        saveTableIndexes(indexesList.get, dataMapToSave)
      }
    }
    return saving
  }

  def update(dataMap: Map[String, String], classId: String): Any = {
    //on transforme l'id reçu au format attendu par Redis
    val key = determineId(classId, Some(dataMap.get("id").get.toLong))

    //on recupere afin de supprimer les anciennes valeurs de cette table dans la table des index
    val indexesString = dataMap.get("index")
    var oldValues: Map[String, String] = Map()

    // on copie le map reçu dans un autre map car element en parametre est toujours val, i.e immutable (inchangeable)
    var updatedDataMap = dataMap
    updatedDataMap += "id" -> key

    if (indexesString != None && indexesString.get != None) {
      for (index <- indexesString.get.split(",")) {
        val oldValue = findPropertiesValues(updatedDataMap.get("id").get, index.trim)
        oldValues = oldValues ++ oldValue.get
      }
      // Console.println("oldValues = " + oldValues)
    }
    var saving = false
    val idValue = updatedDataMap.get("id").get
    if (idValue != None && redisClient.hexists(idValue, "id")) {
      // Console.println("idValue = " + idValue)
      saving = redisClient.hmset(idValue, updatedDataMap)

      // After avoir enregistré dans la table principale, on lance l'enregistrement des indexes dans un Future (un autre thread, en gros, pour que cela ne bloque pas le flux principal)
      Future {
        if (indexesString.get != None) {
          updateTableIndexes(indexesString.get, updatedDataMap, oldValues)
        }
      }
    }
    return saving
  }


  def delete(dataMap: Map[String, String], classId: String, id: String): Any = {
    //on transforme l'id reçu au format attendu par Redis
    val key = determineId(classId, Some(id.toLong))

    //on recupere afin de supprimer les anciennes valeurs de cette table dans la table des index
    val indexesString = dataMap.get("index")
    var oldIndexesValues: Map[String, String] = Map()

    // on copie le map reçu dans un autre map car element en parametre est toujours val, i.e immutable (inchangeable)
    var updatedDataMap = dataMap
    updatedDataMap += "id" -> key

    if (indexesString != None && indexesString.get != None) {
      for (index <- indexesString.get.split(",")) {
        val oldValue = findPropertiesValues(updatedDataMap.get("id").get, index.trim)
        oldIndexesValues = oldIndexesValues ++ oldValue.get
      }
      Console.println("oldIndexesValues = " + oldIndexesValues)
    }

    val rslt = redisClient.del(key)

    //Deleting indexes
    Future {
      if (indexesString.get != None) {
        deleteFromTableIndexes(oldIndexesValues, updatedDataMap.get("id").get)
      }
    }

    return rslt.get
  }


  def findByKey(classId: String, id: String): Any = {
    val key = determineId(classId, Some(id.toLong))
    Console.println("key = " + key)
    val rslt = redisClient.hgetall(key)
    Console.println("rslt = " + rslt)
    return rslt.get
  }


  def findByParams(params: Option[Map[String, Any]]): Any = {

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

  /*
  Methode qui boucle sur le champ 'index' d'un model passé en parametre.
  Pour chaque valeur trouvée, elle cree un enregistrement dans une map de type Set.
   */
  def saveTableIndexes(indexesList: String, dataModel: Map[String, String]) = {
    for (index <- indexesList.split(",")) {
      val indextrim = index.trim
      val valueMap = dataModel.get(indextrim)
      if (valueMap.get != None) {
        val key = indextrim + ":" + valueMap.get
        redisClient.sadd(key, dataModel.get("id").get)
      }
    }
  }

  /*
  Suppression des index d'un model.
  La methode reçoit en params:
   - une map contenant les noms des index et leurs valeurs.
      Ex: [name:Baggio, firstname:Hyoga]
   - l'id du model
      Ex: "users:10"

   */
  def deleteFromTableIndexes(indexesKeyList: Map[String, Any], value: String) = {
    for (index <- indexesKeyList) {
      Console.println("index = " + index)
      val key = index._1 + ":" + index._2
      Console.println("key = " + key)
      Console.println("value = " + value)
      redisClient.srem(key, value)
    }
  }


  /*
  Methode qui boucle sur le champ 'index' d'un model passé en parametre.
  Pour chaque valeur trouvée, elle verifie si le champ en question est concerné par la modification.
   Si non, rien n'est fait. Si oui, elle supprime l'ancien enregistrement puis en cree nouveau dans une map de type Set.
   */
  def updateTableIndexes(indexesList: String, dataModel: Map[String, String], oldIndexesMap: Map[String, String]) = {
    for (index <- indexesList.split(",")) {
      val indextrim = index.trim
      val valueMap = dataModel.get(indextrim)
      val oldValueMap = oldIndexesMap.get(indextrim)
      Console.println("valueMap = " + indextrim + " - " + valueMap)
      Console.println("oldValueMap = " + indextrim + " - " + oldValueMap)
      Console.println("valueMap.get = " + valueMap.get)
      Console.println("oldValueMap.get = " + oldValueMap.get)
      // Si l'ancienne et la nouvelle valeur sont differentes
      if ((oldValueMap != None && oldValueMap.get != None && valueMap != None && valueMap.get != oldValueMap.get) || (oldValueMap != None && oldValueMap.get != None && valueMap == None)) {
        //on supprime l'ancien index
        val oldKey = indextrim + ":" + oldValueMap.get
        Console.println("oldKey = " + oldKey)
        redisClient.srem(oldKey, dataModel.get("id").get)
      }

      //on enregistre le nouvel index
      //TODO On gere pas le cas où l'index n'a jamais été créé dans le Set. Comme sa valeur ne change pas dans le model, il n'est toujours pas créé. Prevoir une methode de reprise, au cas où on creerait un index en cours de roite
      if ((valueMap.get != None && valueMap.get != "" && valueMap.get != oldValueMap.get) || (valueMap.get != None && valueMap.get != "" && oldValueMap == None)) {
        //Console.println("sadd index = "+valueMap)
        val key = indextrim + ":" + valueMap.get
        redisClient.sadd(key, dataModel.get("id").get)
      }
    }
  }


  // TODO hmget prend en compte le nombre de variables. ie, si on veut un champ, on envoie un param. Si on en veut 2, on en envoie 2. Une liste de 2 params ne marcherait pas.
  def findPropertiesValues(id: String, propertyList: String): Option[Map[String, String]] = {

    redisClient.hmget(id, propertyList)
    //redisClient.hmget(id, "firstname", "username", "id")
  }


  def findPropertyValue(id: String, property: String): Any = {
    redisClient.hget(id, property)
  }


}

/*
http://www.michaelpollmeier.com/2014/06/29/simple-dependency-injection-scala/
 */
object RedisHashService extends RedisHashService