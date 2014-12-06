package utilities

/**
 * Created by hyoga on 02/12/2014.
 */
class Constantes {

  /* Le format des dates reçues via rest.
  Les données reçues via rest sont des strings.
  Pour pouvoir parser des données au format Datetime, on se base sur ce pattern.
  De fait, une donnée reçue avec ce format sera successfullment transformée en Date
   */
  final val datePattern = "yyyy-MM-dd HH:mm:ss";
}
