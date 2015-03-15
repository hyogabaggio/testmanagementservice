package services.database.redis

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
