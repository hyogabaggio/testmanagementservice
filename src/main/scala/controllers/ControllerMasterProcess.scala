package controllers

import akka.actor.SupervisorStrategy.Escalate
import akka.actor.{Props, OneForOneStrategy, Actor, ActorRef}
import utilities.Validation

/**
 * Created by hyoga on 31/01/2015.
 */
class ControllerMasterProcess(dbService: ActorRef) extends Actor {


  var booleanResult = Option.empty[Boolean]
  var mapResult = Option.empty[Map[String, String]]
  var listResult = Option.empty[Seq[Map[String, String]]]

  def receive = {
    case data: Map[String, Any] => {

      if (data.isInstanceOf[Map[String, Any]]) {

        // val methodController = getControllerMethod(data)
        val controllerInstance = data("httpcontroller").toString()

        //on recupere les valeurs contenues dans la requete
        val params = data.asInstanceOf[Map[String, Any]]
        //perRequest(ctx, Props(Class.forName(controller).getConstructors()(0).newInstance(dbService).asInstanceOf[Actor]), httpmap)
        val controllerActor = context.actorOf(Props(Class.forName(controllerInstance).getConstructors()(0).newInstance(dbService).asInstanceOf[Actor]))
        controllerActor ! data

        Console.println("after triggerControllerMethod  = ")

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
    case f: Validation => context.parent ! f

    case t: Any => {
      Console.println(" waitingResponses booleanResult  = " + t) // TODO gerer ce cas after
      replyIfReady
    }
  }


  def replyIfReady = {
    Console.println("booleanResult  = " + booleanResult)
    Console.println("booleanResult getClass = " + booleanResult.getClass)
    Console.println("booleanResult nonEmpty = " + booleanResult.nonEmpty)
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
