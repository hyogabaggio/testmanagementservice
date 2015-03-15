package controllers

import akka.actor.ActorRef
import models.{Users}
import net.liftweb.json.Serialization._
import net.liftweb.json.{NoTypeHints, Serialization}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import services.database.DbOperationService
import services.database.redis.RedisHashService
import utilities.Tools
import models.Conversions.domainToAnyRef //important !!!!!

/**
 * Created by hyoga on 23/11/2014.
 */
class UsersController() extends Controller() {

  /*
  Actions: save, show, list, update, delete
   */

  def save(params: Map[String, Any]): Any = {
    var user: Users = new Users()
    //on charge les elements reçus dans le case class Users afin de pouvoir juste manipuler l'objet Users
    // binding(user, mapHttpbody).asInstanceOf[Users]
    extractHttpbodyAndBind(user, params).asInstanceOf[Users]

    Console.println("user = " + user)

    if (user.validate.isEmpty) Console.println("is valide = true")
    else Console.println("is valide = false")
    //  return true
    //val save = db.checkAndSave(user)
    return user.validateAndSave
  }

  def update(params: Map[String, Any]): Any = {
    var user: Users = new Users()
    //on charge les elements reçus dans le case class Users afin de pouvoir juste manipuler l'objet Users
    // binding(user, mapHttpbody).asInstanceOf[Users]
    extractHttpbodyAndBind(user, params).asInstanceOf[Users]

    Console.println("user = " + user)
    return user.validateAndUpdate
  }

  def show(params: Map[String, Any]): Any = {
    Console.println("show params = " + params)
    //TODO ne pas oublier le httptail pour les hasMany
    val user = new Users()
    //var tools = new Tools()

    return user.get(params.get("id").get.asInstanceOf[String])
    // return user.get(params.get("id").toString)
  }


  def list(params: Map[String, Any]): Any = {
    Console.println("date = " + DateTime.now().toString(DateTimeFormat.forPattern("ddMMyyyyHHmmss")))

    //httptail,     httpdomain
    val user = new Users()
   user.get(params.get("httpparams"))
  }

  def delete(params: Map[String, Any]): Any = {
    val user = new Users()
    return user.delete(params.get("id").get.asInstanceOf[String])
  }


}
