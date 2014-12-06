package controllers

import models.{TestQuestion, Users}
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
    Console.println("user dateNaissance = " + user.dateNaissance)
    Console.println("user fullName = " + user.fullName)

    Console.println("user valide = " + user.validate)
    // Console.println("user valide implicit = " + user.validate)


  }


  //Unused
  def assignFromPair(kv: (String, Any), user: Users): Unit = {
    //   Console.println("property = " + kv._1)
    //  Console.println("value = " + kv._2)
    val listFields = user.getClass.getDeclaredFields().map(_.getName)
    //  listFields.foreach(it => Console.println("listFields = " + it))



    if (listFields.contains(kv._1.toString)) {
      /*   var field = user.getClass.getDeclaredField(kv._1.toString)
         field.setAccessible(true)
         Console.println("field = " + field)
         // Console.println("field = " + field.isAccessible + ", class = " + field.getClass)
         field.set(user, kv._2)        */

      user.getSet(user) set(kv._1.toString, kv._2)

      // ne marche que sur les string, et encore, pas sur les option[string]
      //TODO voir http://stackoverflow.com/questions/1589603/scala-set-a-field-value-reflectively-from-field-name

    }
  }


}
