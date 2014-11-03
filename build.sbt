name := "testmanagementservice"

version := "1.0"

scalaVersion := "2.11.2"

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/"
)

libraryDependencies ++= {
  val akkaVersion = "2.3.6"
  val sprayVersion = "1.3.2"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion
      exclude("org.scala-lang", "scala-library"),
    "net.debasishg" %% "redisclient" % "2.13",
    "io.spray" % "spray-can_2.11" % sprayVersion,
    "io.spray" % "spray-routing_2.11" % sprayVersion,
    "net.liftweb" %% "lift-json" % "3.0-M2",
   "com.github.nscala-time" %% "nscala-time" % "1.4.0"
    //"io.spray" %% "spray-json" % "1.2.5" exclude ("org.scala-lang" , "scala-library"),
  )
}


    