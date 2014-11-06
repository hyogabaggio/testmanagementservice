package controllers

/**
 * Created by macbookpro on 28/10/2014.
 */
trait Controller {


  /*
  Methode recuperant tous les elements reçus par une methode d'un controller, sous forme de map.

   */

  //TODO ne pas oublier la sécurité. Essayer de gerer ça façon GrailsFilter

  def receive(params: Map[String, String]) = {
    // recuperation du domain
    val domainName = params.get("httpdomain")
    if (domainName != None) {
      // determination du controller (nom relatif: package.nomClass)
      val controller = "controllers." + domainName.toString().capitalize + "Controller"

      val specificAction = params.get("httpspecificaction")
      //Si une action specifique a été demandée, elle prend le dessus sur les actions HTTP
      if (specificAction != None) {
        // TODO appeller la methode 'callMethod'
      //  callMethod(controller, specificAction, params)
      } else {
        // Pas d'action specifique, donc action http


      }


    }

  }


  /*
  Reflection
  http://stackoverflow.com/questions/2060395/is-there-any-scala-feature-that-allows-you-to-call-a-method-whose-name-is-stored

  Pour chaque modele, on crée un Controller, puis par convention, à chaque modele detecté d'une requete reçue, on instancie le controller associé, et on appelle la methode visée par la requete.
   Pour les cas CRUD, les gerer par convention: GET /users <=> UsersController/list.
   Ne pas oublier de verifier que le Controller existe bien pour le modele envoyé dans la requete
  NB: Pas de package au niveau des Controllers. Je ne sais pas les gerer de maniere efficiente
   */
  def createInstance(clazzName: String) = Class.forName(clazzName).newInstance

  /*
     http://stackoverflow.com/questions/2060395/is-there-any-scala-feature-that-allows-you-to-call-a-method-whose-name-is-stored

     Methode qui instancie une classe (ici un controller) selon son nom relatif (package.nomClass), puis qui invoque une méthode selon son nom envoyé en parametre en lui transmettant des parametres sous forme de Map
   */
  def callMethod(className: String, methodName: String, argsMethod: Map[String, String]): Unit = {
    val controllerInstance = createInstance(className)
    val method = controllerInstance.getClass.getMethod(methodName, argsMethod.getClass)
    method.invoke(controllerInstance, argsMethod)
  }
}
