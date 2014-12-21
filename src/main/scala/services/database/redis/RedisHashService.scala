package services.database.redis

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
  def save(dataMap: Map[String, Any], classId: String): Any = {
    // On incremente la table des ids afin de recuperer le nouvel id pour cet enregistrement
    var idValue: Long = 0
    redisClient.incr(classId).map { id =>
      Console.println("id = " + id)
      idValue = id;
    }
    //On determine l'id à enregistrer, puis on le set au niveau du Map
    val dataMapToSave = setIdToDomain(dataMap, determineId(classId, idValue))
    Console.println("dataMapToSave = " + dataMapToSave)

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
  def determineId(classId: String, idValue: Long): String = {
    var classIdCopy = classId
    if (classId.contains("id")) classIdCopy.replace("id", idValue.toString)
    else {
      classIdCopy += ":" + idValue.toString
    }
    return classIdCopy
  }

  def setIdToDomain(dataMap: Map[String, Any], classId: String): Map[String, Any] = {
    var dataMapCopy = dataMap
    dataMapCopy += "id" -> classId
    return dataMapCopy
  }

}

/*
http://www.michaelpollmeier.com/2014/06/29/simple-dependency-injection-scala/
 */
object RedisHashService extends RedisHashService