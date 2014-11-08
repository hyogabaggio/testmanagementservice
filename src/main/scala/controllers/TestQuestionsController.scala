package controllers

/**
 * Created by hyoga on 27/10/2014.
 */
class TestQuestionsController {
  // TODO herite de Controller ????

  def list(params: Map[String, Any]) = {
    Console.println(" TestQuestionsController list = " + params)
  }

  def show(params: Map[String, Any]) = {
    Console.println(" TestQuestionsController show = " + params)
    Console.println(" TestQuestionsController show id = " + params.get("id"))
    Console.println(" TestQuestionsController show params = " + params.get("httpparams"))
  }

  def save(params: Map[String, Any]) = {
    Console.println(" TestQuestionsController save = " + params)
    Console.println(" TestQuestionsController save httpbody = " + params.get("httpbody"))
    Console.println(" TestQuestionsController save params = " + params.get("httpparams"))
  }

  def update(params: Map[String, Any]) = {
    Console.println(" TestQuestionsController update = " + params)
    Console.println(" TestQuestionsController update httpbody = " + params.get("httpbody"))
    Console.println(" TestQuestionsController update params = " + params.get("httpparams"))
  }

  def delete(params: Map[String, Any]) = {
    Console.println(" TestQuestionsController delete = " + params)
  }



}
