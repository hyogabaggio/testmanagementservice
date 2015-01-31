package services.database.redis


import scala.concurrent.{Promise, Await, Future}
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
    import akka.pattern.after
    // On incremente la table des ids afin de recuperer le nouvel id pour cet enregistrement
    var idValue: Long = 0
    redisClient.incr(classId).map { id =>
      idValue = id;
      Console.println("idValue = " + idValue)
      //On determine l'id à enregistrer, puis on le set au niveau du Map
      val key = determineId(classId, idValue)
      val dataMapToSave = setIdToDomain(dataMap, key)
      Console.println("dataMapToSave = " + dataMapToSave)
      val execSaving = redisClient.hmset(key, dataMapToSave)
      /*  val mapResult = for {
          result <- redisClient.hmset(key, dataMapToSave).mapTo[Boolean]
        } yield result  */
      /*  val delayed = after(timeout.duration, using = system.scheduler)(Future.failed(new IllegalStateException("OHNOES")))
        val future = Future {
          Thread.sleep(3000);
          true;
        }     */
      Console.println("execSaving before = " + execSaving.value)
      val result = Promise[Boolean]
      execSaving onComplete {
        case Success(true) => result.success(true)
        case Failure(error) => result.failure(/*new Exception("database.redis.error")*/error)
      }

     Console.println("rslt = " + result.future.value)

       result.future
      Console.println("rslt after = " + result.future.value)
      Console.println("execSaving after = " + execSaving.value)


      // return result

    }

  }


  def findByKey(classType: String, id: String): Any = {
    val key = classType.toLowerCase + ':' + id
    Console.println("key = " + key)
    redisClient.hgetall(key).map { rslt =>
      Console.println("rslt = " + rslt)
    }
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
  def determineId(classId: String, idValue: Long): String = {
    var classIdCopy = classId
    if (classId.contains("id")) classIdCopy = classIdCopy.replace("id", idValue.toString)
    else {
      classIdCopy += ":" + idValue.toString
    }
    return classIdCopy
  }

  def setIdToDomain(dataMap: Map[String, String], classId: String): Map[String, String] = {
    var dataMapCopy = dataMap
    dataMapCopy += "id" -> classId
    return dataMapCopy
  }

}

/*
http://www.michaelpollmeier.com/2014/06/29/simple-dependency-injection-scala/
 */
object RedisHashService extends RedisHashService