name := "testmanagementservice"

version := "1.0"

scalaVersion := "2.11.2"

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/"
)

// TODO remplacer lift-json par json4s-native: https://github.com/json4s/json4s

libraryDependencies ++= {
  val akkaVersion = "2.3.6"
  val sprayVersion = "1.3.2"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion
      exclude("org.scala-lang", "scala-library"),
    "io.spray" % "spray-can_2.11" % sprayVersion,
    "io.spray" % "spray-routing_2.11" % sprayVersion,
    "net.liftweb" %% "lift-json" % "3.0-M2",
    "org.json4s" %% "json4s-native" % "3.2.11",
    "com.github.nscala-time" %% "nscala-time" % "1.4.0",
    "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
    "net.debasishg" %% "redisclient" % "2.13",
    "org.scalikejdbc" %% "scalikejdbc" % "2.2.3",
    "org.scalikejdbc" %% "scalikejdbc-config"  % "2.2.3",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "mysql" % "mysql-connector-java" % "5.1.12"
   // "net.debasishg" %% "redisreact" % "0.7"
    //"io.spray" %% "spray-json" % "1.2.5" exclude ("org.scala-lang" , "scala-library"),
  )
}


