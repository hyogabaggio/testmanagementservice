package models

/**
 * Created by hyoga on 15/03/2015.
 *
 * Tables subsidiaires créees:
 * - toutes les valeurs de l'index (val index) (table de type 'set', ex: 'etablissements:firstname:hyoga' 'users:10')
 * - toutes jointures: les references vers d'autres models (table de type 'set', ex: 'etablissements:etablissments:1' 'users:10')
 * - 'etablissements:createdAt' (table de type 'sortedset', ex: 'etablissements:createdAt' '08032015155154' 'etablissements:10' (date au format ddMMyyyyHHmmss)
 */
case class Etablissements(var id: Long,
                          var intitule: String,
                          var abrege: String,
                          var nomComplet: String,
                          var adresse: String,
                          var tel: String,
                          val pays: String,
                          var siteweb: String) extends Domain {

  val redisStructure = "hash"
  val redisId = "etablissements:id"

  override def toString() = nomComplet

  // 2nd constructeur
  def this() = this(0, null, null, null, null, null, null, null)

  // verif des contraintes
  def checkConstraint(propertyName: String) = propertyName match {
    case "intitule" if intitule == "" || intitule == null => ("intitule" -> "etablissements.intitule.not.blank.error")
    case _ => ("none", "none")
  }


}
