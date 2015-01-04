package controllers

import models.{TestQuestion, Users}
import net.liftweb.json.Serialization._
import net.liftweb.json.{NoTypeHints, Serialization}
import services.database.DbOperationService
import utilities.Tools
import models.Conversions.domainToAnyRef

/**
 * Created by hyoga on 23/11/2014.
 */
class UsersController extends Controller {

  /*
  Actions: save, show, list, update, delete
   */

  def save(params: Map[String, Any]): Any = {
    var user: Users = new Users()
    //on charge les elements reçus dans le case class Users
    // binding(user, mapHttpbody).asInstanceOf[Users]
    extractHttpbodyAndBind(user, params).asInstanceOf[Users]

    Console.println("user = " + user)

    if (user.validate.isEmpty) Console.println("is valide = true")
    else Console.println("is valide = false")
    //val save = db.checkAndSave(user)
    val save = user.validateAndSave
    Console.println("save = " + save)
    return save
  }

  def update(params: Map[String, Any]): Any = {


  }

  def show(params: Map[String, Any]):Any = {
    Console.println("show params = " + params)
  }


}
