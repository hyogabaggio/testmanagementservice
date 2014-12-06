package models

import org.joda.time.DateTime


/**
 * Created by hyoga on 23/11/2014.
 * Class representant un compte utilisateur
 */
case class Users(//var id: Option[Long],
                 var id: Long,
                 var name: String,
                 var firstname: String,
                 var email: String,
                 var password: String,
                 // var tel: Option[Long],
                 var tel: Int,
                 // var sexe: Option[String],
                 var sexe: String,
                 //  var fullName: Option[String],
                 var fullName: String,
                 var isOnline: Boolean,
                 var dateNaissance: DateTime) extends Domain {

  //TODO module de recup des infos depuis un compte mail, facebook, twitter

  //  second constructeur
  def this() = this(0, null, null, null, null, 0, null, null, false, null)

  /*require(name != "" && name != null, "users.name.not.blank.error")
  require(firstname != "" && firstname != null, "users.firstname.not.blank.error")
  require(email != "" && email != null, "users.firstname.not.blank.error")
  require(password != "" && password != null, "users.password.not.blank.error")
  require(/*sexe != "" && sexe != null &&*/ List("H", "F").contains(sexe), "users.sexe.not.valid.error")    */


  /*
  Methode de verification de contraintes.
  Selon la propriété qui est envoyée, des contraintes sont vérifiées.
  Si la valeur est valide, 'none' est renvoyée.
  Sinon, la propriété, ainsi que l'erreur rencontrée sont renvoyées.
   */
  def checkConstraint(propertyName: String) = propertyName match {
    case "name" if name == "" || name == null => ("name" -> "users.name.not.blank.error")
    case "firstname" if firstname == "" || firstname == null => ("firstname" -> "users.firstname.not.blank.error")
    case "email" if email == "" || email == null => ("email" -> "users.email.not.blank.error")
    case "password" if password == "" || password == null => ("password" -> "users.password.not.blank.error")
    case "sexe" if (!List("H", "F").contains(sexe)) => ("sexe" -> "users.sexe.not.valid.error")
    case "dateNaissance" if dateNaissance == null => ("dateNaissance" -> "users.dateNaissance.not.nullable.error")
    case _ => ("none", "none")


  }

  //ToString
  override def toString() = fullName

  // generating fullName
  this.fullName = this.name + this.firstname


}
