import sbt.Project.projectToLocalProject

// Projects
lazy val core = project
  .settings(commonSettings)
  .settings(
    libraryDependencies += dependencies.`cats-effect`,
    libraryDependencies ++= dependencies.circe
  )

lazy val `fs-blobstore-layer` = project
  .in(file("layers/blobstore"))
  .settings(commonSettings)
  .settings(
    libraryDependencies += dependencies.`fs2-blobstore-core`
  )
  .dependsOn(core)

lazy val `scalacache-layer` = project
  .in(file("layers/scalacache"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      dependencies.`scalacache-core`,
      dependencies.`scalacache-cats-effect`
    )
  )
  .dependsOn(core)

lazy val sample = project
  .settings(commonSettings)
  .settings(
    libraryDependencies += "com.github.cb372" %% "scalacache-caffeine" % "0.28.0"
  )
  .dependsOn(core, `fs-blobstore-layer`, `scalacache-layer`)

lazy val root = project
  .in(file("."))
  .settings(name := "layer4s")
  .settings(commonSettings)
  .aggregate(core)

// Settings
lazy val commonSettings = Seq(
  scalaVersion := "2.13.4",
  crossScalaVersions := Seq("2.12.12", "2.13.4"),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.11.3" cross CrossVersion.full),
  organization := "de.thatscalaguy",
  organizationName := "ThatScalaGuy (Sven Herrmann)",
  startYear := Some(2021),
  licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "thatscalaguy",
      "Sven Herrmann",
      "sven.herrmann@thatscalaguy.de",
      url("https://github.com/thatscalaguy")
    )
  ),
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
  ),
)

// Dependencies
lazy val dependencies = new {
  object Versions {
    val `cats-effect` = "2.3.1"
    val `fs2-blobstore` = "0.7.3"
    val circe = "0.12.3"
    val scalacache = "0.28.0"
  }

  val `cats-effect` = "org.typelevel" %% "cats-effect" % Versions.`cats-effect`

  val `fs2-blobstore-core` = "com.github.fs2-blobstore" %% "core"  % Versions.`fs2-blobstore`

  val `scalacache-core` = "com.github.cb372" %% "scalacache-core" % Versions.scalacache
  val `scalacache-cats-effect` = "com.github.cb372" %% "scalacache-cats-effect" % Versions.scalacache


  val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % Versions.circe)
}