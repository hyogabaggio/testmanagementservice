package controllers

import models.{TestQuestion, Users}
import net.liftweb.json.Serialization._
import net.liftweb.json.{NoTypeHints, Serialization}
import services.database.DbOperationService
import utilities.Tools
import models.Conversions.domainToAnyRef

/**
 * Created by macbookpro on 23/11/2014.
 */
class UsersController extends Controller {

  def save(params: Map[String, Any]) = {
    // Console.println(" UsersController save = " + params)
    //  Console.println(" UsersController save = " + params.get("httpbody"))
    //  Console.println(" UsersController class = " + params.get("httpbody").getClass)

    val tools = new Tools
    // Console.println(" UsersController getType  = " + tools.getType(params.get("httpbody")))
    //  Console.println(" UsersController getType getClass = " + tools.getType(params.get("httpbody")).getClass)
    var user: Users = new Users()

    var mapHttpbody = tools.getType(params.get("httpbody")).asInstanceOf[Map[String, Any]]
    binding(user, mapHttpbody).asInstanceOf[Users]

    Console.println("user = " + user)
    Console.println("user name = " + user.name)
    Console.println("user firstname = " + user.firstname)
    Console.println("user dateNaissance = " + user.dateNaissance)
    Console.println("user id = " + user.id)
    Console.println("user isOnline = " + user.isOnline)
    Console.println("user fullName = " + user.fullName)
    Console.println("user redisStructure = " + user.redisStructure)

    Console.println("user valide = " + user.validate)
    if (user.validate.isEmpty) Console.println("is valide = true")
    else Console.println("is valide = false")
    val db: DbOperationService = new DbOperationService()
    db.checkAndSave(user)

    // Console.println("user valide implicit = " + user.validate)


  }


}
