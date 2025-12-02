val scala3Version = "3.7.4"

val tapirVersion          = "1.12.3"
val jsoniterMacrosVersion = "2.30.1"
val magnumVersion         = "1.3.1"
val hikariCPVersion       = "6.3.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "narrative-analytics",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"           %% "tapir-core"              % tapirVersion,
      "com.softwaremill.sttp.tapir"           %% "tapir-jsoniter-scala"    % tapirVersion,
      "com.softwaremill.sttp.tapir"           %% "tapir-cats-effect"       % tapirVersion,
      "com.softwaremill.sttp.tapir"           %% "tapir-netty-server-cats" % tapirVersion,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros"   % jsoniterMacrosVersion,
      "com.augustnagro"                       %% "magnum"                  % magnumVersion,
      // Java deps
      "com.zaxxer"                             % "HikariCP"                % hikariCPVersion
    ),
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
  )
