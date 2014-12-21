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
                 var tel: Int,
                 var sexe: String,
                 var fullName: String,
                 var isOnline: Boolean,
                 var dateNaissance: DateTime) extends Domain {

  //TODO module de recup des infos depuis un compte mail, facebook, twitter

  //  second constructeur
  def this() = this(0, null, null, null, null, 0, null, null, false, null)


  /*  Cette propriété represente le type de structure dans lequel ce modele est stocké dans Redis.
  Redis a des structures, des "types de tables", dans lesquelles elle stocke les données.
  Chaque structure a des propriétés differentes, et surtout des commandes differentes.
  Donc il faut par avance connaitre cette structure avant de pouvoir effectuer des requetes dessus
      => http://redis.io/commands
   */
  val redisStructure = "hash"

  /* Redis ne gere pas les incrementations automatiques de clé (id)
   Cette prorpiété represente la clé ou est stocké la derniere valeur de l'id de cette table.
   Avant chaque save, on m'incremente, et la nouvelle valeur devient l'id
   */
  val redisId = "users:id"


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
  //this.fullName = this.name + this.firstname


}
