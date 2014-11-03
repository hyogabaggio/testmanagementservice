package controllers

/**
 * Created by macbookpro on 28/10/2014.
 */
trait Controller {


  /*
  Methode recuperant tous les elements reçus par une methode d'un controller, sous forme de map.
  Elle identifie le controller, définit la methode du controller visée et lui envoie le map
   */

  def receive(params: Map[String, String]) = {
    // recuperation du domain
    val domainName = params.get("httpdomain")
    if (domainName != None) {
      // determination du controller
      val controller = domainName.toString().capitalize + "Controller"


      val specificAction = params.get("httpspecificaction")
      if (specificAction != None) {
                   // TODO appeller la methode 'callMethod'
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

     Methode qui instancie une classe (ici un controller) selon son nom relatif (package.nomClasse), puis qui invoque une méthode selon son nom envoyé en parametre en lui transmettant des parametres sous forme de Map
   */
  def callMethod(className: String, methodName: String, argsMethod: Map[String, String]): Unit = {
    val controllerInstance = createInstance(raw"controllers.$controller")
    val method = controllerInstance.getClass.getMethod(methodName, argsMethod.getClass)
    method.invoke(controllerInstance, argsMethod)
  }
}
