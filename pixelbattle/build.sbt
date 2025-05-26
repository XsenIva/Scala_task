val scala213Version = "2.13.14"
val catsEffectVersion = "3.5.3"
val akkaVersion = "2.9.2"
val akkaHttpVersion = "10.6.2"
val circeVersion = "0.14.7"

lazy val root = project
  .in(file("."))
  .settings(
    name := "pixelBattle",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala213Version,
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/",
    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "42.7.1",

      // Play Framework
      "com.typesafe.play" %% "play" % "2.8.20",

      // Doobie
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC2",
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC2",

      // Swagger
      "io.swagger.core.v3" % "swagger-core" % "2.2.8",
      "io.swagger.core.v3" % "swagger-annotations" % "2.2.8",
      "io.swagger.core.v3" % "swagger-models" % "2.2.8",

      // Flyway & logging
      "org.flywaydb" % "flyway-core" % "9.16.0",
      "ch.qos.logback" % "logback-classic" % "1.4.14",

      // Testing
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % Test,
      "com.h2database" % "h2" % "2.2.224" % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "org.mockito" %% "mockito-scala" % "1.17.30" % Test,
      "org.mockito" %% "mockito-scala-scalatest" % "1.17.30" % Test,

      // Akka
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-protobuf-v3" % akkaVersion,

      // Circe
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,

      // Akka HTTP Circe интеграция
      "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",

      // Cats Effect
      "org.typelevel" %% "cats-effect" % catsEffectVersion
    ),

    dependencyOverrides ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-protobuf-v3" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "org.typelevel" %% "cats-core" % "2.10.0",
      "org.typelevel" %% "cats-kernel" % "2.10.0",
      "org.typelevel" %% "jawn-parser" % "1.5.0"
    )
  )
