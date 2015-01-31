package models

import services.database.DbOperationService
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


  /*
  Methode de validation des models.
  Elle est plus une methode d'accés. Elle boucle sur l'ensemble des propriétés du model, et appelle la methode de validation (checkValidation) sur chacune d'elles.
   */
  def validateDomain(implicit classInstance: AnyRef): Map[String, String] = {
    var errorsMap: Map[String, String] = Map()
    val listFields = classInstance.getClass.getDeclaredFields().map(_.getName)
    listFields.map(field => {
      if (checkValidation(classInstance, field)._1 != "none") errorsMap += checkValidation(classInstance, field)
    })
    return errorsMap
  }

  /*
     Methode qui transforme un Domain en Map[String, String]
   */
  def domainToMap(implicit DomainInstance: AnyRef): Map[String, String] = {
    (Map[String, String]() /: DomainInstance.getClass.getDeclaredFields) { (a, f) =>
      f.setAccessible(true)
      a + (f.getName -> {
        if (f.get(DomainInstance) != null) f.get(DomainInstance).toString
        else "null"
      })
    }
  }

  /*
     Methode qui transforme un Domain en Map[String, Any]
   */
  def domainToMapOfAny(implicit DomainInstance: AnyRef): Map[String, Any] = {
    (Map[String, Any]() /: DomainInstance.getClass.getDeclaredFields) { (a, f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(DomainInstance))
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

  /*
  Methode qui valide d'abord le modele, puis qui l'enregistre dans la bdd
   */
  def checkAndSave(classInstance: AnyRef): Any = {
   // val db: DbOperationService = new DbOperationService
   // return db.checkAndSave(classInstance)
              //TODO regler le DbOperationService en actor
  }


  /*
  Methode qui recherche un enregistrement via l'ID
   */
  //TODO proteger l'acces à ces methodes par protected ou private
    def getById(classInstance: AnyRef, id: String): Any = {
  /*  val db: DbOperationService = DbOperationService
     Console.println("classInstance getName = "+classInstance.getClass.getSimpleName)
    db.getById(classInstance, classInstance.getClass.getSimpleName, id)    */
    //TODO regler le DbOperationService en actor
  }


}

class DomainAnyRef(anyClass: AnyRef) {

  def validate(): Map[String, String] = {
    //  Console.println("is instance of ? = "+anyClass.isInstanceOf[Domain])
    if (anyClass.isInstanceOf[Domain]) {
      val domain = anyClass.asInstanceOf[Domain]
      return domain.validateDomain(domain)
    } else Map("Domain" -> "class.not.a.Domain")

  }

  def toMap(): Map[String, String] = {
    if (anyClass.isInstanceOf[Domain]) {
      val domain = anyClass.asInstanceOf[Domain]
      return domain.domainToMap(domain)
    } else Map("Domain" -> "class.not.a.Domain")
  }

  def toMapOfAny(): Map[String, Any] = {
    if (anyClass.isInstanceOf[Domain]) {
      val domain = anyClass.asInstanceOf[Domain]
      return domain.domainToMapOfAny(domain)
    } else Map("Domain" -> "class.not.a.Domain")
  }


  def validateAndSave(): Any = {
    if (anyClass.isInstanceOf[Domain]) {
      val domain = anyClass.asInstanceOf[Domain]
      return domain.checkAndSave(domain)
    } else Map("Domain" -> "class.not.a.Domain")
  }

   def get(id: String): Any = {
    if (anyClass.isInstanceOf[Domain]) {
      val domain = anyClass.asInstanceOf[Domain]
      return domain.getById(domain, id)
    }
  }
}

object Conversions {

  /*Tranforme un AnyRef en DomainAnyRef
  Et permet donc d'appliquer au AnyRef les methodes de DomainAnyRef
  Ex: DomainAnyRef a une methode validate(), avec cette conversion, on peut faire AnyRef.validate()
  */
  implicit def domainToAnyRef(anyClass: AnyRef) = new DomainAnyRef(anyClass)

}
