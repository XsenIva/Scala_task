val scala3Version = "3.6.3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "pixelBattle",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "org.postgresql" % "postgresql" % "42.7.1",

  
      "com.typesafe.akka" %% "akka-http" % "10.2.10",
      "com.typesafe.akka" %% "akka-stream" % "2.6.20",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.10", 
      "com.typesafe.akka" %% "akka-http-websockets" % "10.2.10",

 
      "com.typesafe.play" %% "play" % "2.8.18",


      "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC2", 
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC2",


      "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.8.0",
      "io.swagger.core.v3" % "swagger-core" % "2.2.8",
      "io.swagger.core.v3" % "swagger-annotations" % "2.2.8", 
      "io.swagger.core.v3" % "swagger-models" % "2.2.8", 


      "org.flywaydb" % "flyway-core" % "9.16.0",

      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
        "org.mockito" %% "mockito-scala" % "1.17.12" % Test, 
      "com.typesafe.akka" %% "akka-http-testkit" % "10.2.10" % Test

    )
  
  
  )
