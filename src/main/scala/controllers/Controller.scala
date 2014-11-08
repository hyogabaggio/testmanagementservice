package controllers

/**
 * Created by hyoga on 28/10/2014.
 *
 * Trait (pseudo interface sous scala) hérité par tout Controller.
 * La methode Receive reçoit tous les appels vers un controller depuis le routage.
 * Elle instancie le controller visé, appelle la methode et lui envoie les données en parametres sous forme de Map.
 *
 * Elle gere aussi la sécurité (authentification)
 */
trait Controller {


  /*
  Methode recuperant tous les elements reçus par une methode d'un controller, sous forme de map.

   */

  //TODO ne pas oublier la sécurité. Essayer de gerer ça façon GrailsFilter

  def receive(params: Map[String, Any]) = {
    // recuperation du controller visé
    val controller = params("httpcontroller").toString()
    val specificAction = params.get("httpactionspecific")
    // Console.println("Controller specificAction = " + specificAction)
    //Si une action specifique a été demandée, elle prend le dessus sur les actions HTTP
    if (specificAction != None) {
      callMethod(controller, specificAction.toString(), params)
    } else {
      // Pas d'action specifique, donc action http
      callMethod(controller, params("httpaction").toString(), params)

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
  def callMethod(className: String, methodName: String, argsMethod: Map[String, Any]): Unit = {
    val classInstance = createInstance(className)
    val method = classInstance.getClass.getMethod(methodName, classOf[Map[String, Any]])
    method.invoke(classInstance, argsMethod)
  }
}
