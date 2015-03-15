package controllers

import models.Etablissements
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import models.Conversions.domainToAnyRef

/**
 * Created by hyoga on 15/03/2015.
 */
class EtablissementsController extends Controller() {

  /*
  Actions: save, show, list, update, delete
   */

  def save(params: Map[String, Any]): Any = {
    Console.println("date = " + DateTime.now().toString(DateTimeFormat.forPattern("ddMMyyHHmmss")))
    var etablissement: Etablissements = new Etablissements()
    extractHttpbodyAndBind(etablissement, params).asInstanceOf[Etablissements]

    Console.println("etablissement = " + etablissement)

    if (etablissement.validate.isEmpty) Console.println("is valide = true")
    else Console.println("is valide = false")
    return etablissement.validateAndSave
  }

  def update(params: Map[String, Any]): Any = {
    var etablissement: Etablissements = new Etablissements()
    extractHttpbodyAndBind(etablissement, params).asInstanceOf[Etablissements]

    return etablissement.validateAndUpdate
  }

  def show(params: Map[String, Any]): Any = {
    Console.println("show params = " + params)
    //TODO ne pas oublier le httptail pour les hasMany
    var etablissement: Etablissements = new Etablissements()
    return etablissement.get(params.get("id").get.asInstanceOf[String])
  }


  def list(params: Map[String, Any]): Any = {
    var etablissement: Etablissements = new Etablissements()
    etablissement.get(params.get("httpparams"))
  }

  def delete(params: Map[String, Any]): Any = {
    var etablissement: Etablissements = new Etablissements()
    return etablissement.delete(params.get("id").get.asInstanceOf[String])
  }


}
