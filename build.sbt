name := "users"
version := "1.0.0"

lazy val Http4sVersion  = "0.18.0-M4"
lazy val CirceVersion   = "0.9.0-M1"

scalaVersion := "2.12.3"
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-Ypartial-unification"
)

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "com.softwaremill.quicklens"    %% "quicklens"      % "1.4.11",
  "org.typelevel"                 %% "cats-core"      % "1.0.0-MF",
  "org.typelevel"                 %% "cats-effect"    % "0.4",

  "org.http4s"        %% "http4s-blaze-server"          % Http4sVersion,
  "org.http4s"        %% "http4s-blaze-client"          % Http4sVersion,
  "org.http4s"        %% "http4s-dsl"                   % Http4sVersion,
  "org.http4s"        %% "http4s-circe"                 % Http4sVersion,
  "io.circe"          %% "circe-core"                   % CirceVersion,
  "io.circe"          %% "circe-generic"                % CirceVersion,

  compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
)
