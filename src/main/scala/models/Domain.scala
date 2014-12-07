package models

import com.sun.tools.internal.xjc.api.util.ToolsJarNotFoundException
import utilities.{Constantes, Tools}

/**
 * Created by hyoga on 30/11/2014.

Tout model doit heriter de Domain.
Domain contient des helpers generiques, pouvant être appliquées à tout modele.
 */
trait Domain {

  /*
  Methode permettant de setter et de getter des valeurs aux propriétés d'un modele "à l'aveugle", i.e sans les ecrire.
  En gros elle permet un binding depuis une map par exemple. On boucle sur la map, et pour chaque key representant le nom de la propriété, on set sa valeur representée par la value de la map.
   */
  implicit def getSet(ref: AnyRef) = new {
    // On recupere la liste des propriétés de la classe
    // Avant un get ou un set, on s'assure d'abord que la propriété existe bien dans le modele
    val listFields = ref.getClass.getDeclaredFields().map(_.getName)

    def get(name: String): Any = {
      if (listFields.contains(name))
        ref.getClass.getMethods.find(_.getName == name).get.invoke(ref)
    }

    def set(name: String, value: Any): Unit = {
      if (listFields.contains(name)) {
        import utilities.Conversions.stringToStringComplements
        val tools = new Tools
        val property = ref.getClass.getDeclaredField(name)
        // Console.println("property = " + property)
        val propertyType = property.getType.getSimpleName
        // Console.println("propertyType = " + propertyType)

        propertyType match {

          case "String" => ref.getClass.getMethods.find(_.getName == name + "_$eq").get.invoke(ref, value.asInstanceOf[AnyRef])
          case "long" if value.toString.isNumber => ref.getClass.getMethods.find(_.getName == name + "_$eq").get.invoke(ref, value.toString.toLong.asInstanceOf[AnyRef])
          case "int" if value.toString.isNumber => ref.getClass.getMethods.find(_.getName == name + "_$eq").get.invoke(ref, value.toString.toInt.asInstanceOf[AnyRef])
          case "float" if value.toString.isNumber => ref.getClass.getMethods.find(_.getName == name + "_$eq").get.invoke(ref, value.toString.toFloat.asInstanceOf[AnyRef])
          case "double" if value.toString.isNumber => ref.getClass.getMethods.find(_.getName == name + "_$eq").get.invoke(ref, value.toString.toDouble.asInstanceOf[AnyRef])
          case "short" if value.toString.isNumber => ref.getClass.getMethods.find(_.getName == name + "_$eq").get.invoke(ref, value.toString.toShort.asInstanceOf[AnyRef])
          case "boolean" if value.toString.isBoolean => ref.getClass.getMethods.find(_.getName == name + "_$eq").get.invoke(ref, value.toString.toBoolean.asInstanceOf[AnyRef])
          case "DateTime" if value.toString.isDateTime => ref.getClass.getMethods.find(_.getName == name + "_$eq").get.invoke(ref, value.toString.toDateTime().asInstanceOf[AnyRef])
          case _ => None
          // TODO voir d'autres types à gerer"
        }

      }
    }
  }


  def validateDomain(implicit classInstance: AnyRef): Map[String, String] = {
    var errorsMap: Map[String, String] = Map()
    val listFields = classInstance.getClass.getDeclaredFields().map(_.getName)
    listFields.map(field => {
      if (checkValidation(classInstance, field)._1 != "none") errorsMap += checkValidation(classInstance, field)
    })
    return errorsMap
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

class DomainAnyRef(anyClass: AnyRef) {

  def validate(): Map[String, String] = {
    val domain = anyClass.asInstanceOf[Domain]
    return domain.validateDomain(domain)
  }
}

object Conversions {

  //Tranforme un AnyRef en DomainAnyRef
  // Et permet donc d'appliquer au AnyRef les methodes de DomainAnyRef
  // Ex: DomainAnyRef a une methode isBoolean, avec cette conversion, on peut faire AnyRef.isBoolean
  implicit def domainToAnyRef(anyClass: AnyRef) = new DomainAnyRef(anyClass)

}
