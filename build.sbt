name := "finatra-scaffold"

version := "0.0.1"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Twitter Maven" at "https://maven.twttr.com",
  "Finatra Repo" at "http://twitter.github.com/finatra"
)

val finatraVer = "2.7.0"

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % finatraVer,
  "com.twitter" %% "finatra-httpclient" % finatraVer,
  "com.twitter" %% "finatra-slf4j" % finatraVer,
  "com.twitter" %% "inject-core" % finatraVer,
  "ch.qos.logback" % "logback-classic" % "1.1.9",
  "org.reactivemongo" %% "reactivemongo" % "0.11.14",
  "com.github.xiaodongw" %% "swagger-finatra" % "0.7.1",
  "com.lihaoyi" % "sourcecode_2.11" % "0.1.3",

  "com.google.inject.extensions" % "guice-testlib" % "4.0" % "test",
  "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.0.0" % "test",
  "org.mockito" % "mockito-core" % "2.6.5" % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.specs2" %% "specs2" % "3.7" % "test",
  "com.twitter" %% "finatra-http" % finatraVer % "test",
  "com.twitter" %% "finatra-jackson" % finatraVer % "test",
  "com.twitter" %% "inject-server" % finatraVer % "test",
  "com.twitter" %% "inject-app" % finatraVer % "test",
  "com.twitter" %% "inject-core" % finatraVer % "test",
  "com.twitter" %% "inject-modules" % finatraVer % "test",
  "com.twitter" %% "finatra-http" % finatraVer % "test" classifier "tests",
  "com.twitter" %% "finatra-jackson" % finatraVer % "test" classifier "tests",
  "com.twitter" %% "inject-server" % finatraVer % "test" classifier "tests",
  "com.twitter" %% "inject-app" % finatraVer % "test" classifier "tests",
  "com.twitter" %% "inject-core" % finatraVer % "test" classifier "tests",
  "com.twitter" %% "inject-modules" % finatraVer % "test" classifier "tests"
)
