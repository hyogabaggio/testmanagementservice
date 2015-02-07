package routes

import akka.actor.{Props, Actor}
import controllers.{ControllerMasterProcess, Controller}
import models._
import net.liftweb.json.Serialization._
import net.liftweb.json._
import org.joda.time.format.DateTimeFormat
import services.database.DbOperationService
import spray.http._
import spray.routing._
import utilities.Failure

class RoutingActor extends Actor with RoutingService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  implicit def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing,
  // timeout handling or alternative handler registration
  def receive = runRoute(testmanagementRoute)

}

/**
 * La methode de routage récupere l'adresse de toute requete entrante.
 * A partir de cette adresse, elle tire la méthode HTTP, le domaine ciblé, l'id (s'il y en a), la "queue" (trail) et les parametres (params).
 * Ex: GET 'http://127.0.0.1:8080/etudiant/2/inscriptionEtudiant/2?etab=1&etuId=2'
 * domain: etudiant;
 * id: 2;
 * trail: /inscriptionEtudiant/2;
 * params: Map(etab -> 1, etuId -> 2).
 *
 * Le but du routage est de localiser le Controller de destination, d'identifier la méthode ciblée et lui envoyer le "reste" de l'adresse.
 * Le 'domain' correspond à une classe dans le package 'models'. Il correspond aussi à une table dans la bdd.
 * A chaque 'domain' corresond un Controller, créé dans le package 'controllers'.
 *
 * NB: il n'ya pas de sous-package dans le package controllers, même dans le cas où il yen aurait dans 'models'.
 *
 * Le controller permet l'acces au model (instanciation) ainsi que vers les differents services ou vers la bdd.
 * Il contient des actions, correspondant, soit à une methode HTTP, soit à des actions specifiques.
 * Methodes HTTP:
 * Créer (create) => POST 'http://127.0.0.1:8080/domain/'
 * Afficher (read - list) => GET 'http://127.0.0.1:8080/domain/'
 * Afficher (read - show) => GET 'http://127.0.0.1:8080/domain/id'
 * Mettre à jour (update) => PUT 'http://127.0.0.1:8080/domain/id'
 * Supprimer (delete) => DELETE 'http://127.0.0.1:8080/domain/id'
 *
 * Les methodes specifiques sont celles créees par le dev.
 * GET 'http://127.0.0.1:8080/domain/searchAction?name=te&username=te&adresse=dakar'
 * Par convention, ces methodes doivent finir par 'Action'. Sinon, le Systeme les prendra comme des domains imbriqués.
 *
 * Une fois le controller et l'action localisés, le routage leur envoie toutes les informations (id, trail, params).
 * Si le trail ne commence pas par '/XXXXXAction' (donc si ce n'est pas une actionç l'interieur du controller, il s'agit donc d'un autre domain (imbriqué), le systeme renvoie juste le restant (trail + params) au Routage.
 * A partir du trail, le systeme localise le controller concerné par le domain imbriqué et lui envoie le reste du trail + les params. Et ainsi de suite.
 *
 **/

trait RoutingService extends HttpService with Actor with ControllerPerRequestCreator {
  // we use the enclosing ActorContext's or ActorSystem's dispatcher for our Futures and Scheduler
  implicit def executionContext = actorRefFactory.dispatcher

  implicit val formats = DefaultFormats

 // val dbService = actorRefFactory.actorOf(Props[DbOperationService])

  implicit val customRejectionHandler = RejectionHandler {
    case rejections => mapHttpResponse {
      response =>
        response.withEntity(HttpEntity(ContentType(MediaTypes.`application/json`),
          write(Map("error" -> response.entity.asString))))
    } {
      RejectionHandler.Default(rejections)
    }
  }

  //val relativeExract = extract(_.request.uri.toRelative)
  val pathExtract = extract(_.request.uri.path)

  /* methode qui prend une adresse en argument une url (en string) et en retire le domaine concerné
  Ex: localhost:8080/users => users
   */
  protected def domainExtract(value: String): Directive1[String] = provide {
    val path = value.split("/").toList
    path match {
      case p@(_ :: _) => p(1) // si le size de la list >=1
      case _ => "http://localhost:8080/"

    }
  }


  //TODO inserer un logger (log4j ou autre)

  //val fmt = DateTimeFormat.forPattern("yyMMddHHmmssSSS")
  //possiblement inutile
  val testmanagementRoute = respondWithMediaType(MediaTypes.`application/json`) {
    pathPrefix("api") {
      //TODO prendre en compte le /api/testmanagementservice/ ds le httprequest reçu dans la  methode addToMap
      pathPrefix("testmanagementservice") {
        detach() {
          ctx => // on recupere le contexte global
            // On recupere l'ensemble des données contenues dans la requete et on les charge dans un map.
            // Tout le reste de l'application ne manipulera que ce map
            //  Console.println("ctx.request= " + ctx.request)
            val httpmap = httpToMap(ctx.request)
            // On envoie httpmap au 'Controller Global'. Lui se chargera d'identifier le controller visé et de lui envoyer les données

            // TODO gerer les valeurs retournées par Controller.receive
            //  val controller = new Controller {}
            // controller.triggerControllerMethod(httpmap)
           /* val controller = httpmap("httpcontroller").toString()
            // Console.println("sendMessageToController = " + controller)
           //  perRequest(ctx, Props(Class.forName(controller).getConstructors()(0).newInstance(dbService).asInstanceOf[Actor]), httpmap) */
            perRequest(ctx, Props(new ControllerMasterProcess()), httpmap)

            //TODO finir la methode avec perRequest, i.e effacer tout ce qui vient aprés cette ligne
           /* var domain = httpmap.get("httpdomain")
            var method = httpmap.get("httpmethod").toString().toUpperCase()
            //TODO partout, retourner en erreur si: pas httpmap.httpaction et httpactionspecific, pas httpmap.httpcontroller
            //TODO gerer les erreurs avec customRejectionHandler

            ctx.complete(raw"$method $domain OK  ")    */
        }
      }
    }
  }

  //inutilisé
  def sendMessageToController(message: Map[String, Any]): Route = {
    ctx =>
      Console.println("sendMessageToController getClass = " + ctx.getClass)
      val controller = message("httpcontroller").toString()
      Console.println("sendMessageToController = " + controller)
    //  perRequest(ctx, Props(Class.forName(controller).getConstructors()(0).newInstance(dbService).asInstanceOf[Actor]), message)
      perRequest(ctx, Props(Class.forName(controller).newInstance().asInstanceOf[Actor]), message)


  }


  /*
  Methode qui reçoit un httpRequest et le "tansforme" en map.
   Il insere toutes les données du httpRequest dans un map.
   */
  // TODO http://codereview.stackexchange.com/questions/10030/osgi-like-infrastructure
  def httpToMap(httpRequest: HttpRequest): Map[String, Any] = {
    var contentMap: Map[String, Any] = Map()

    // Ajout de la methode http
    contentMap += "httpmethod" -> httpRequest.method.name // inutile

    // ajout des params (si existant)
    //Ex: 'http://127.0.0.1:8080/api/testmanagementservice/domain/searchAction?name=te&username=te&adresse=dakar' => name=te&username=te&adresse=dakar
    if (httpRequest.uri.query.nonEmpty) {
      val params = httpRequest.uri.query.toMap

      var paramsMap: Map[String, Any] = Map()
      for (x <- params) {
        paramsMap = addToMap(x, paramsMap)
      }
      contentMap += "httpparams" -> paramsMap

    }

    // ajout du body (si existant, (POST, PUT))
    if (httpRequest.entity.data.nonEmpty) {
      //on transforme le body en JSON
      val json = parse(httpRequest.entity.data.asString).values.asInstanceOf[Map[String, String]]

      // on ajoute les elements du body au map
      var bodyMap: Map[String, Any] = Map()
      for (x <- json) {
        bodyMap = addToMap(x, bodyMap)
      }
      contentMap += "httpbody" -> bodyMap
    }

    // ajout de l'id (/users/12 => 12) et de l'urltail (/users/12/userroles/43 => /userroles/43)
    if (httpRequest.uri.path.isEmpty == false) {
      // recup du domain
      //Ex: http://127.0.0.1:8080/domain => domain
      val domain = extractDomainFromPath(httpRequest.uri.path)
      if (domain.isEmpty == false) {
        //ajout du domain
        contentMap += "httpdomain" -> domain //possiblement inutile

        //ajout du controller
        //le controller aura un nom relatif: package.nomClass= controllers.DomainController
        val controller = "controllers." + domain.toString().capitalize + "Controller"
        contentMap += "httpcontroller" -> controller
      }

      //recuperarion de la methode specifique (si elle existe)
      //Ex: 'http://127.0.0.1:8080/domain/searchAction?name=te&username=te&adresse=dakar' => searchAction
      // Par convention, elle doit se terminer par 'Action'
      val actionspecific = extractSpecificActionFromPath(httpRequest.uri.path)
      if (actionspecific.isEmpty == false) contentMap += "httpactionspecific" -> actionspecific

      // recup de l'id
      val id = extractIdFromPath(httpRequest.uri.path)
      if (id.isEmpty == false) contentMap += "id" -> id

      //recup de l'urltail
      val tail = extractTailFromPath(httpRequest.uri.path)
      if (tail.isEmpty == false) contentMap += "httptail" -> tail
    }

    //determination de l'action de destination dans le controller
    //Si une action specifique a été demandée (httpactionspecific), elle prend le dessus sur les actions HTTP (httpaction)
    /*
 Les actions sont, soit specifiée par la requete (en "dur"), soit "sous-entendue" par la methode http.

 En dur: clé "httpspecificaction" du map. Si elle existe, elle a la priorité.
 Sous-entendu: définie par la methode http (get, post, put, delete) et les params envoyés.

   GET:
     - si id envoyé, action << show >>
     Ex: GET 'http://127.0.0.1:8080/users/20'
     - si pas d'id, action << list >>
     Ex: GET 'http://127.0.0.1:8080/users'

   POST:
     - si pas d'id, action << save >>
     Ex: POST 'http://127.0.0.1:8080/users/' // avec le contenu dans le body de la requete
     - si id envoyé, action << update >>
     Ex: POST 'http://127.0.0.1:8080/users/20' // avec le contenu dans le body de la requete

   PUT:
     action << update >>
     Ex: PUT 'http://127.0.0.1:8080/users/20' // avec le contenu dans le body de la requete

   DELETE:
     action << delete >>
     Ex: DELETE 'http://127.0.0.1:8080/domain/id'
  */
    val actionspecific = contentMap.get("httpactionspecific")
    val id = contentMap.get("id")
    if (actionspecific == None) {
      contentMap("httpmethod") match {
        case "GET" => if (id != None) {
          contentMap += "httpaction" -> "show"
        } else {
          contentMap += "httpaction" -> "list"
        }
        case "POST" => if (id != None) {
          contentMap += "httpaction" -> "update"
        } else {
          contentMap += "httpaction" -> "save"
        }
        case "PUT" => if (id != None) {
          contentMap += "httpaction" -> "update"
        }
        case "DELETE" => if (id != None) {
          contentMap += "httpaction" -> "delete"
        }
      }
    }

    //Console.println("contentMap = " + contentMap)

    return contentMap

  }


  // methode qui ajoute un map à un autre map
  def addToMap(pair: (String, String), map: Map[String, Any]): Map[String, Any] = {
    map + (pair._1 -> pair._2)
  }

  def stringAsMap(pair: (String, String)): String = {
    pair._1 + ":" + pair._2 + ";"
  }


  /*
  Methode qui reçoit en params une Path (depuis un httpRequest.uri.path), et en tire l'id inclus
  Ex:  /users/12/userroles/43 => 12
   */
  def extractDomainFromPath(path: Uri.Path): String = {
    val values = path.toString().split("/").toList
    values match {
      case p@(_ :: _ :: _ :: _ :: _) => if (isInteger(p(3)) == false) {
        // si le size de la list >=3
        // on prend 3 car l'adresse est de la forme http://127.0.0.1:8080/api/testmanagementservice/testQuestions => testQuestions
        p(3)
      } else {
        ""
      }
      case _ => ""
    }
  }


  /*
  Methode qui reçoit en params une Path (depuis un httpRequest.uri.path), et en tire l'id inclus
  Ex:  /users/12/userroles/43 => 12
   */
  def extractIdFromPath(path: Uri.Path): String = {
    val values = path.toString().split("/").toList
    values match {
      case p@(_ :: _ :: _ :: _ :: _ :: _) => if (isInteger(p(4))) {
        // si le size de la list >=5
        //Ex: x :: y :: xs matches lists of length ≥2, binding x to the list's first element, y to the list's second element, and xs to the remainder.
        p(4)
      } else if (p(4).endsWith("Action")) {
        if (p.size >= 5) {
          if (isInteger(p(5))) p(5)
          else ""
        } else {
          ""
        }
      } else {
        ""
      }
      case _ => ""
    }
  }

  /*
  Methode qui reçoit une path en params (depuis un httpRequest.uri.path) et en tire le "tail".
  Tail = restant du path apres avoir enlevé le domain et l'id.
  Ex: /users/12/userroles/43 => /userroles/43
  Ex: /users/userroles/43 => /userroles/43
   */

  def extractTailFromPath(path: Uri.Path): String = {
    val index = extractTailStartIndexFromPath(path)

    index match {
      case index: Int if index > 0 => path.toString().substring(index)
      case _ => ""
    }
  }

  /*
Methode qui reçoit une path en params (depuis un httpRequest.uri.path) et en tire l'index du "tail".
Tail = restant du path apres avoir enlevé le domain et l'id.
Ex: /users/12/userroles/43 => /userroles/43
Ex: /users/userroles/43 => /userroles/43
Ex: /users/searchAction/43 => / (lorsque le params apres le domain se termine par 'Action', il s'agit d'une action spécifique)
*/
  def extractTailStartIndexFromPath(path: Uri.Path): Int = {
    // on recupere l'index du debut du trail
    val values = path.toString().split("/").toList
    values match {
      case p@(_ :: _ :: _ :: _ :: _ :: _ :: _) => if (isInteger(p(4))) {
        //Ex: x :: y :: xs matches lists of length ≥2, binding x to the list's first element, y to the list's second element, and xs to the remainder.
        path.toString().indexOf(p(5))
      } else if (p(4).endsWith("Action")) {
        if (isInteger(p(5))) {
          if (p.size >= 6) path.toString().indexOf(p(6))
          else 0
        } else {
          path.toString().indexOf(p(5))
        }
      } else {
        path.toString().indexOf(p(4))
      }
      case _ => 0
    }
  }

  /*
  Methode qui reçoit une path en params (depuis un httpRequest.uri.path) et en tire une action specifique.
  Une action specifique vient juste aprés le domain et se termine tjrs par 'Action'
  Ex: /users/searchAction/43 => 'searchAction'
   */
  def extractSpecificActionFromPath(path: Uri.Path): String = {
    val values = path.toString().split("/").toList
    values match {
      case p@(_ :: _ :: _ :: _ :: _ :: _) => if (p(4).endsWith("Action")) {
        // si le size de la list >=5
        //Ex: x :: y :: xs matches lists of length ≥2, binding x to the list's first element, y to the list's second element, and xs to the remainder.
        p(4)
      } else {
        ""
      }
      case _ => ""
    }
  }

  // Check if a string is a number
  def isInteger(x: String): Boolean = {
    x.forall(_.isDigit)
  }


  def unmarshaller(msg: String, objectType: String) = {
    //    val json = parse(msg)
    //                                val test = json.values.asInstanceOf[Map[String, Any]]
    //    Console.println("json users= " + test)
    //    Console.println("json users= " + test.getClass())
    Console.println("msg users= " + msg)
    objectType match {
      case "testQuestion" => read[TestQuestion](msg)
      case "failure" => read[Failure](msg)
      //     case _ => None
    }
  }


  def unmarshaller2(msg: String, ObjectType: String): TestQuestion = {

    implicit val formats = Serialization.formats(NoTypeHints)
    Console.println("msg users= " + msg)
    //    var ex = "{\"testQuestion\":{\"libelle\": \"question 1\", \"groupe\":\"ei\"}}"
    //    Console.println("msg ex= " + ex)
    read[TestQuestion](msg)
    //    val json = parse(msg)
    //    json.extract[TestQuestion]
  }


  /*
  !!!!!!!!!! Soluce !!!!!!!!!!
  Reflection
  http://stackoverflow.com/questions/2060395/is-there-any-scala-feature-that-allows-you-to-call-a-method-whose-name-is-stored

  Pour chaque modele, on crée un Controller, puis par convention, à chaque modele detecté d'une requete arrivante, on instancie le controller associé, et on appelle la methode visée par la requete.
   Pour les cas CRUD, les gerer par convention: GET /users <=> UsersController/list.
   Ne pas oublier de verifier que le Controller existe bien pour le modele envoyé dans la requete
   NB: Ne jamais, jamais deserialiser dans le RoutingActor. Il ne sert qu'à choisir la bonne methode et appeller le bon Controller.
   Pas de package au niveau des Controllers. Ne sait pas les gerer de maniere efficiente
   */
  def createInstance(clazzName: String) = Class.forName(clazzName).newInstance


  /**
   * Handles an incoming request and create valid response for it.
   *
   * @param ctx         request context
   * @param successCode HTTP Status code for success
   * @param action      action to perform
   */
  protected def handleRequest(ctx: RequestContext, successCode: StatusCode = StatusCodes.OK)(action: => Either[Failure, _]) {
    action match {
      case Right(result: Object) =>
        ctx.complete(successCode, write(result))
      case Left(error: Failure) =>
        ctx.complete(error.getStatusCode, net.liftweb.json.Serialization.write(Map("error" -> error.message)))
      case _ =>
        ctx.complete(StatusCodes.InternalServerError)
    }
  }


}