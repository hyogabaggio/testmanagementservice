package routes

import java.util.concurrent.TimeUnit

import akka.actor.SupervisorStrategy.Stop
import akka.actor._
//import net.liftweb.json.DefaultFormats

//import net.liftweb.json.DefaultFormats
import routes.ControllerPerRequestActor.{WithProps, WithActorRef}
import spray.http.StatusCode
import spray.http.StatusCodes._
import spray.httpx.Json4sSupport
import org.json4s.DefaultFormats
import spray.routing.RequestContext
import scala.concurrent.duration._
import utilities.{Error, Validation}
//import org.json4s.DefaultFormats

/**
 * Created by hyoga on 18/01/2015.
 */
trait ControllerPerRequestActor extends Actor with Json4sSupport{

  import context._

   val json4sFormats = DefaultFormats

  def ctx: RequestContext

  def target: ActorRef

  def message: Map[String, Any]

 // setReceiveTimeout(2, TimeUnit.SECONDS)
  setReceiveTimeout(2.seconds)
  target ! message

  def receive = {
    case res: Option[Map[String, Any]] => complete(OK, res)
    case res: Option[Boolean] => complete(OK, res)
    case res: Option[Seq[Map[String, String]]] => complete(OK, res)
    case v: Validation => complete(BadRequest, v)
    case ReceiveTimeout => complete(GatewayTimeout, Error("Request timeout"))
  }

  def complete[T <: AnyRef](status: StatusCode, obj: T) = {
    ctx.complete(status, obj)
    stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        complete(InternalServerError, Error(e.getMessage))
        Stop
      }
    }
}


object ControllerPerRequestActor {

  case class WithActorRef(ctx: RequestContext, target: ActorRef, message: Map[String, Any]) extends ControllerPerRequestActor

  case class WithProps(ctx: RequestContext, props: Props, message: Map[String, Any]) extends ControllerPerRequestActor {
    lazy val target = context.actorOf(props)
  }

}

trait ControllerPerRequestCreator {
  this: Actor =>
  //constructeurs
  def perRequest(ctx: RequestContext, target: ActorRef, message: Map[String, Any]) =
    context.actorOf(Props(new WithActorRef(ctx, target, message)))

  def perRequest(ctx: RequestContext, props: Props, message: Map[String, Any]) =
    context.actorOf(Props(new WithProps(ctx, props, message)))

}
