val scala3Version = "3.7.4"

val tapirVersion          = "1.12.3"
val jsoniterMacrosVersion = "2.30.1"
val magnumVersion         = "1.3.1"
val hikariCPVersion       = "6.3.0"
val log4catsVersion       = "2.7.1"
val logbackVersion        = "1.5.18"
val macwireVersion        = "2.6.6"
val postgresVersion       = "42.7.7"
val pureconfigVersion     = "0.17.9"
val scaffeineVersion      = "5.3.0"

// Test deps
val munitVersion     = "1.0.0"
val munitCatsVersion = "2.1.0"
val scalaMockVersion = "7.5.1"

lazy val commonSettings = Seq(
  version      := "0.1.0-SNAPSHOT",
  scalaVersion := scala3Version
)


lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .dependsOn(webdef)
  .settings(
    name := "narrative-analytics",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"           %% "tapir-cats-effect"       % tapirVersion,
      "com.softwaremill.sttp.tapir"           %% "tapir-netty-server-cats" % tapirVersion,
      "com.softwaremill.sttp.tapir"           %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.augustnagro"                       %% "magnum"                  % magnumVersion,
      "org.typelevel"                         %% "log4cats-slf4j"          % log4catsVersion,
      "com.softwaremill.macwire"              %% "macros"                  % macwireVersion,
      "com.github.pureconfig"                 %% "pureconfig-core"         % pureconfigVersion,
      "com.github.blemale"                    %% "scaffeine"               % scaffeineVersion,
      // Java deps
      "ch.qos.logback"                         % "logback-classic"         % logbackVersion,
      // Database
      "org.postgresql"                         % "postgresql"              % postgresVersion,
      "com.zaxxer"                             % "HikariCP"                % hikariCPVersion
    ),
    libraryDependencies ++= Seq(
      "org.scalameta"               %% "munit"                   % munitVersion     % Test,
      "org.scalamock"               %% "scalamock"               % scalaMockVersion % Test,
      "org.typelevel"               %% "munit-cats-effect"       % munitCatsVersion % Test,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub4-server" % tapirVersion     % Test
    ),
    Compile / run / fork := true
  )


// Web definitions that can later be exported for clients to consume
lazy val webdef = project
  .in(file("webdef"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"           %% "tapir-core"              % tapirVersion,
      "com.softwaremill.sttp.tapir"           %% "tapir-jsoniter-scala"    % tapirVersion,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros"   % jsoniterMacrosVersion
    )
  )
