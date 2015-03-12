lazy val commonSettings = Seq(
  organization := "edu.tamu.idhmc",
  organizationName := "Early Modern OCR Project",
  organizationHomepage := Some(url("http://emop.tamu.edu")),
  startYear := Some(2012),
  licenses += "Apache2" -> url("http://www.apache.org/licenses/LICENSE-2.0"),
  scalaVersion := "2.11.6",
  scalacOptions ++= Seq("-feature", "-language:postfixOps", "-target:jvm-1.7")
)

lazy val `page-corrector` = (project in file(".")).
  enablePlugins(JavaAppPackaging).
  settings(commonSettings: _*).
  settings(
    name := "page-corrector",
    version := "1.10.0-SNAPSHOT",
    description := "Applies rule-based, context-based, and dictionary-based corrections to hOCR documents from Tesseract.",
    libraryDependencies ++= Seq(
        "org.rogach"                    %% "scallop"                % "0.9.5",
        "mysql"                         %  "mysql-connector-java"   % "5.1.34",
        "org.xerial"                    %  "sqlite-jdbc"            % "3.8.7",
        "com.jolbox"                    %  "bonecp"                 % "0.8.0.RELEASE",
        // "edu.illinois.i3.spellcheck"    %  "Jazzy"                  % "1.4-0.5.2",
        "net.liftweb"                   %% "lift-json"              % "2.6",
        "com.jsuereth"                  %% "scala-arm"              % "1.4",
        // "edu.illinois.i3.scala"         %% "scala-utils"            % "20150130.1-SNAPSHOT",
        "com.typesafe.scala-logging"    %% "scala-logging"          % "3.1.0",
        "ch.qos.logback"                %  "logback-classic"        % "1.1.2",
        "org.scalatest"                 %% "scalatest"              % "2.2.1"   % Test
      )
  )
