package models

/**
 * Created by hyoga on 30/11/2014.

Tout model doit heriter de Domain.
Domain contient des helpers generiques, pouvant être appliquées à tout modele.
 */
trait Domain {

  /*
  Methode permettant de setter et de getter des valeurs aux propriétés d'un modele "à l'aveugle", i.e sans les ecrire.
  En gros elle permet un binding depuis une map par exemple. On bouclerait sur la map, et pour chaque key representant le nom de la propriété, on setterait sa valeur reepresentée par la value de la map.
   */
  implicit def getSet(ref: AnyRef) = new {
    def get(name: String): Any = ref.getClass.getMethods.find(_.getName == name).get.invoke(ref)

    def set(name: String, value: Any): Unit = {
      val property = ref.getClass.getDeclaredField(name)
      Console.println("property = " + property)
      val propertyType = property.getType.getSimpleName
      propertyType match {
        case "String" => ref.getClass.getMethods.find(_.getName == name + "_$eq").get.invoke(ref, value.asInstanceOf[AnyRef])
        case _ => None
        // TODO gerer les autres types: long, dateTime, option, int,
      }

    }
  }


  def validate(implicit classInstance: AnyRef): Boolean = {
    var errorsMap: Map[String, String] = Map()
    val listFields = classInstance.getClass.getDeclaredFields().map(_.getName)
    listFields.map(field => {
      if (checkValidation(classInstance, field)._1 != "none") errorsMap += checkValidation(classInstance, field)
    })
    for (err <- errorsMap) {
      Console.println("error = " + err)
    }
    Console.println("error size = " + errorsMap.size)

    errorsMap.size match {
      case 0 => true
      case _ => false
    }
  }


  /*
  Methode de vérification des contraintes.
  Les propriétés sont vérifiées une par une (via la methode 'checkConstraint').
  Pour chacune, si une erreur est rencontrée, le nom de la propriété ainsi que le msg d'erreur sont enregistrés dans une map.
  Apres la vérification de toutes les propriétés, la map est renvoyée.
   */
  //TODO essayer de l'integrer avec la methode apply()
  def checkValidation(classInstance: AnyRef, propertyName: String): (String, String) = {

    val method = classInstance.getClass.getMethods.find(_.getName == "checkConstraint")
    // Console.println("method = " + method)
    method match {
      case None => ("none", "none")
      case _ => classInstance.getClass.getMethods.find(_.getName == "checkConstraint").get.invoke(classInstance, propertyName).asInstanceOf[(String, String)]
    }

  }
}
