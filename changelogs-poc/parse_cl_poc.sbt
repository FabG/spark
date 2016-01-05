name := "Change Logs POC"
version := "0.1"
scalaVersion := "2.10.4"
//scalaVersion := "2.11.7"

// additional libraries
libraryDependencies ++= Seq(
 "org.apache.spark" %% "spark-core" % "1.5.2",
 "org.apache.spark" %% "spark-sql" % "1.5.2"
)
