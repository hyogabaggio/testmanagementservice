package models

/**
 * Created by hyoga on 19/10/2014.
 *
 * Modele des questions qui doivent être posées durant le test psychologique.
 * Le id est nullable(signifié par le Option) car il est generé par la bdd.
 * Le 2nd constructeur permet de pouvoir instancier le modele sans avoir forcement à l'initialiser (un peu comme les domains de Grails)
 */
case class TestQuestion(var id: Option[Long],
                        var libelle: String,
                        var groupe: String) {

  //  second constructeur
  def this() = this(null, null, null)

  //ToString
  override def toString() = libelle

  /*
  Methode de vérification des contraintes.
  Les propriétés sont vérifiées une par une (via la methode 'checkConstraint').
  Pour chacune, si une erreur est rencontrée, le nom de la propriété ainsi que le msg d'erreur sont enregistrés dans une map.
  Apres la vérification de toutes les propriétés, la map est renvoyée.
   */
  def checkValidation(): Map[String, String] = {
    var errorsMap: Map[String, String] = Map()

    if (this.checkConstraint("libelle", libelle)._1 != "none") errorsMap += this.checkConstraint("libelle", libelle)
    if (this.checkConstraint("groupe", groupe)._1 != "none") errorsMap += this.checkConstraint("groupe", groupe)

    return errorsMap
  }

  /*
  Methode de verification de contraintes.
  Selon la propriété qui est envoyée, des contraintes sont vérifiées.
  Si la valeur est valide, 'none' est renvoyée.
  Sinon, la propriété, ainsi que l'erreur rencontrée sont renvoyées.
   */
  def checkConstraint(propertyName: String, value: Any) = propertyName match {
    case "libelle" if value == "" || value == null => ("libelle" -> "testQuestion.libelle.not.blank.error")
    case "groupe" if value == "" || value == null => ("groupe" -> "testQuestion.groupe.not.blank.error")
    case _ => ("none", "none")
  }

}
