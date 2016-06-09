import sbt.Keys._
import sbtprotobuf.{ProtobufPlugin => JavaPB}
import com.trueaccord.scalapb.{ScalaPbPlugin => ScalaPB}

val commonSettings = Seq(
  organization := "org.example",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.11.8"
)
val protocVersion = "-v261"

// contains proto files shared between common-java and common-scala
lazy val commonProtobuf =
  (project in file("common-protobuf")).
    settings(
      commonSettings,
      name := "common-protobuf",
      crossPaths := false,
      autoScalaLibrary := false,
      // add protobuf files to generated jar
      unmanagedResourceDirectories in Compile <+= (sourceDirectory in Compile) { _ / "protobuf" }
    )

// generates java code from the proto files in common-protobuf and common-java
// proto files in this module are only compiled to Java classes
lazy val commonJava =
  (project in file("common-java")).
    settings(
      commonSettings,
      name := "common-java",
      JavaPB.protobufSettings,
      sourceDirectories in JavaPB.protobufConfig <+= (sourceDirectory in (commonProtobuf, Compile)) { _ / "protobuf" },
      JavaPB.includePaths in JavaPB.protobufConfig <++= (sourceDirectories in JavaPB.protobufConfig) map (identity(_)),
      JavaPB.runProtoc in JavaPB.protobufConfig := (args =>
        com.github.os72.protocjar.Protoc.runProtoc(protocVersion +: args.toArray)),
      crossPaths := false, // No scala version necessary
      autoScalaLibrary := false, // No Scala library for Android
      // Java 1.7 for Android compatibility
      javacOptions in (Compile, Keys.compile) ++= Seq("-source", "1.7", "-target", "1.7"),
      libraryDependencies ++= Seq(
        "com.google.protobuf" % "protobuf-java" % "3.0.0-beta-2" % JavaPB.protobufConfig
      )
    ).
    dependsOn(commonProtobuf)

// generates Scala code from the proto files in common-protobuf and common-java
lazy val commonScala =
  (project in file("common-scala")).
    settings(
      commonSettings,
      name := "common-scala",
      ScalaPB.protobufSettings,
      sourceDirectories in ScalaPB.protobufConfig <+= (sourceDirectory in (commonProtobuf, Compile)) { _ / "protobuf" },
      ScalaPB.includePaths in ScalaPB.protobufConfig <++= (sourceDirectories in ScalaPB.protobufConfig) map (identity(_)),
      ScalaPB.runProtoc in ScalaPB.protobufConfig := (args =>
        com.github.os72.protocjar.Protoc.runProtoc(protocVersion +: args.toArray)),
      ScalaPB.flatPackage in ScalaPB.protobufConfig := true, // don't append proto file name as package
      libraryDependencies  ++= Seq(
        "com.trueaccord.scalapb" %% "scalapb-runtime" % "0.5.27" % ScalaPB.protobufConfig
      )
    ).
    dependsOn(commonProtobuf)

lazy val root =
  (project in file(".")).
    settings(commonSettings: _*).
    aggregate(commonProtobuf, commonJava, commonScala)
