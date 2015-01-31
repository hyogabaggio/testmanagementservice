package controllers

import akka.actor.SupervisorStrategy.Escalate
import akka.actor.{OneForOneStrategy, Props, ActorRef, Actor}
import models.{Domain, Users}
import utilities.{Validation, Tools}

/**
 * Created by hyoga on 28/10/2014.
 *
 * Trait (pseudo interface sous scala) hérité par tout Controller.
 * La methode Receive reçoit tous les appels vers un controller depuis le routage.
 * Elle instancie le controller visé, appelle la methode et lui envoie les données en parametres sous forme de Map.
 *
 * Elle gere aussi la sécurité (authentification)
 */
class Controller(dbService: ActorRef) extends Actor {

  var booleanResult = Option.empty[Boolean]
  var mapResult = Option.empty[Map[String, String]]
  var listResult = Option.empty[Seq[Map[String, String]]]

  def receive = {
    case data: Map[String, Any] => {
      val methodController = getControllerMethod(data)

      if (data.isInstanceOf[Map[String, Any]]) {

        //on recupere les valeurs contenues dans la requete
        val params = data.asInstanceOf[Map[String, Any]]
        val controller = params("httpcontroller").toString()
        Console.println("this  = " + this.getClass())
        Console.println("super  = " + super.getClass)
        val controllerInstance = this //TODO faire le test avec plusieurs controllers pour confirmer.
        //on execute la methode appelée (save, show, list, update, delete ou une action spécifique)
        triggerControllerMethod(controllerInstance, params) // TODO faut-il que cet appel soit via un acteur pour avoir une reponse via le context ?
        Console.println("after triggerControllerMethod  = ")

        context.become(waitingResponses)
      }
    }

  }

  /*
  Methode recuperant tous les elements reçus par une methode d'un controller, sous forme de map.

   */

  //TODO ne pas oublier la sécurité. Essayer de gerer ça façon GrailsFilter

  def triggerControllerMethod(params: Map[String, Any]) = {
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


  def triggerControllerMethod(controllerInstance: Any, params: Map[String, Any]) = {
    // recuperation du controller visé
    val controller = controllerInstance
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
    if (method != None) method.invoke(classInstance, argsMethod)
  }

  /*
  Même methode que précédemment (même principe d'appel de methode via un variable. Mais ici la classe est déjà instanciée
   */
  def callMethod(classInstance: Any, methodName: String, argsMethod: Map[String, Any]): Unit = {

    val method = classInstance.getClass.getMethod(methodName, classOf[Map[String, Any]])
    if (method != None) method.invoke(classInstance, argsMethod)
  }

  /*
  Methode qui renvoie le nom de la methode du controlleur qui doit être attaquée en recevant une map du RoutingActor.
  La map contient toutes les données envoyées par le client vers l'API, dont le nom du controller ainsi que la methode visée
   */
  def getControllerMethod(params: Map[String, Any]): String = {
    val specificAction = params.get("httpactionspecific")
    // Console.println("Controller specificAction = " + specificAction)
    //Si une action specifique a été demandée, elle prend le dessus sur les actions HTTP
    if (specificAction != None) {
      return specificAction.toString()
    } else {
      // Pas d'action specifique, donc action http
      return params("httpaction").toString()
    }
  }


  /*
  Methode permettant de faire un binding entre une map et un modele.
  Un modele est une case class contenue dans le package Models.
  La map contient un ensemble de paires. Chaque pair etant le nom de propriété en key et la valeur de la propriété en value.

  Ex: Model: userInstance:User(nom:String, prenom:String)
  Map: map:Map(nom -> "Baggio", prenom -> "Roby")
  bindingFromMap(map, userInstance)
    => userInstance.nom = Baggio,
    => userInstance.prenom = Roby
   */
  def binding(classInstance: AnyRef, params: Map[String, Any]): AnyRef = {
    val tools = new Tools
    tools.binding(classInstance, params)
    //params.map(kv => bindingFromPair(kv, classInstance))
    return classInstance
  }

  /*
  Même methode que binding.
  Sauf que le params qui vient contient une map "httpbody". Il faut d'abord l'extraire, puis le binder.
   */
  def extractHttpbodyAndBind(classInstance: AnyRef, params: Map[String, Any]): AnyRef = {
    val tools = new Tools
    tools.extractHttpbodyAndBind(classInstance, params)
    /* var mapHttpbody = tools.getType(params.get("httpbody")).asInstanceOf[Map[String, Any]]
     mapHttpbody.map(kv => bindingFromPair(kv, classInstance))  */
    return classInstance
  }


  /*
  Dès que la methode 'Receive' déclenchée arrive à sa 'fin' (context.become(waitingResponses)), cette methode 'waitingResponses' est appelée, et recevra les reponses reçues des appels effectués par 'Receive'.
  Tous les appels de 'Receive' sont vers le Controller. Donc les methodes appelées sont save, show, list, update, delete ainsi que les methodes spécifiques.
  Pour le moment, on suppose que les types de retour sont:
    - Boolean: methodes de sauvegarde dans la bdd: save, update, delete
    - Map[String, String]: methodes ne renvoyant qu'un enregistrement dans la bdd (get): show
    - List[Map[String, String]]: methodes de recherche dans la bdd, et renvoyant plusieurs résultats: list

    TODO veiller à confirmer cette hypothese avec les méthodes specifiques

    'replyIfReady' retourne les reponses à l'envoyeur
   */
  def waitingResponses: Receive = {
    case booleanresult: Boolean => {
      booleanResult = Some(booleanresult)
      replyIfReady
    }
    case mapresult: Map[String, String] => {
      mapResult = Some(mapresult)
      replyIfReady
    }
    case listresult: Seq[Map[String, String]] => {
      listResult = Some(listresult)
      replyIfReady
    }
    case f: Validation => context.parent ! f
  }


  def replyIfReady = {
    Console.println("booleanResult  = " + booleanResult)
    Console.println("mapResult  = " + mapResult)
    Console.println("listResult  = " + listResult)

    if (booleanResult.nonEmpty) context.parent ! booleanResult
    else if (mapResult.nonEmpty) context.parent ! mapResult
    else if (listResult.nonEmpty) context.parent ! listResult
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }

}
