package utilities

/**
 * Created by hyoga on 24/11/2014.
 */
class Tools {


  def getType(element: Option[Any]): Any = {
    element match {
      case Some(x: Any) => x
      case _ => None
    }
  }


}
