package controllers

import akka.actor.ActorRef
import models.{Users}
import net.liftweb.json.Serialization._
import net.liftweb.json.{NoTypeHints, Serialization}
import services.database.DbOperationService
import utilities.Tools
import models.Conversions.domainToAnyRef

/**
 * Created by hyoga on 23/11/2014.
 */
class UsersController() extends Controller() {

  /*
  Actions: save, show, list, update, delete
   */

  /*def receive = {
    case data => {
      if (data.isInstanceOf[Map[String, Any]]) {

        //on recupere les valeurs contenues dans la requete
        val params = data.asInstanceOf[Map[String, Any]]
        //on execute la methode appelée (save, show, list, update, delete ou une action spécifique)
        triggerControllerMethod(this, params)

       // context.become(waitingResponses)
      }
    }
  }    */

  def save(params: Map[String, Any]): Any = {
    var user: Users = new Users()
    //on charge les elements reçus dans le case class Users
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


  }

  def show(params: Map[String, Any]): Any = {
    Console.println("show params = " + params)
    val user = new Users()
    var tools = new Tools()
    return user.get(tools.getType(params.get("id")).asInstanceOf[String])
   // return user.get(params.get("id").toString)
  }


}
