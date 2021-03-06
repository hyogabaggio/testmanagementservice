package controllers

import akka.actor.SupervisorStrategy.Escalate
import akka.actor.{Props, OneForOneStrategy, Actor, ActorRef}
import utilities.Validation

/**
 * Created by hyoga on 31/01/2015.
 */
class ControllerMasterProcess(/*dbService: ActorRef*/) extends Actor {


  var booleanResult = Option.empty[Boolean]
  var integerResult = Option.empty[Int]
  var longResult = Option.empty[Long]
  var stringResult = Option.empty[String]
  var mapResult = Option.empty[Map[String, String]]
  var listResult = Option.empty[Seq[Map[String, String]]]

  def receive = {
    case data: Map[String, Any] => {

      if (data.isInstanceOf[Map[String, Any]]) {

        // val methodController = getControllerMethod(data)
        val controllerInstance = data("httpcontroller").toString()

        //on recupere les valeurs contenues dans la requete
        val params = data.asInstanceOf[Map[String, Any]]
        //val controllerActor = context.actorOf(Props(Class.forName(controllerInstance).getConstructors()(0).newInstance(dbService).asInstanceOf[Actor]))
        val controllerActor = context.actorOf(Props(Class.forName(controllerInstance).newInstance.asInstanceOf[Actor]))
        controllerActor ! data

        context.become(waitingResponses)
      }
    }

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
    case integerresult: Int => {
      integerResult = Some(integerresult)
      replyIfReady
    }
    case longresult: Long => {
      longResult = Some(longresult)
      replyIfReady
    }
    case stringresult: String => {
      stringResult = Some(stringresult)
      replyIfReady
    }
    case f: Validation => {
      context.parent ! f
    }

    case t: Any => {
      Console.println(" waitingResponses anyResult  = " + t) // TODO gerer ce cas after
      Console.println(" waitingResponses anyResult class = " + t.getClass)
      replyIfReady
    }
  }


  def replyIfReady = {
    if (booleanResult.nonEmpty) Console.println("booleanResult  = " + booleanResult)
    if (mapResult.nonEmpty) Console.println("mapresult = " + mapResult)
    if (listResult.nonEmpty) Console.println("listResult  = " + listResult)
    if (integerResult.nonEmpty) Console.println("integerResult  = " + integerResult)
    if (longResult.nonEmpty) Console.println("longResult  = " + longResult)
    if (stringResult.nonEmpty) Console.println("stringResult  = " + stringResult)

    if (booleanResult.nonEmpty) context.parent ! booleanResult
    else if (integerResult.nonEmpty) context.parent ! integerResult
    else if (longResult.nonEmpty) context.parent ! longResult
    else if (stringResult.nonEmpty) context.parent ! stringResult
    else if (mapResult.nonEmpty) context.parent ! mapResult
    else if (listResult.nonEmpty) context.parent ! listResult
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }

}
