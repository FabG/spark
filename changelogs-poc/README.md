Simple Scala POC to parse a TSV File (Change Logs)
and count number of times a contact field appears

Code:
Available in the following directory:
./src/main/scala

Input Change Logs file:
Available in the following directory:
./input

Compilation:
We are going to packge our Scala project to a .jar file with sbt
The command:
> sbt package
will produce the main artifact as a jar into target/scala-2.x.y/project_name_2.x.y-zz.jar

Our application depends on the Spark API
so we’ll also include an sbt configuration file: 
./parse_cl_poc.sbt

This file also adds a repository that Spark depends on:
name := "Change Logs POC"
version := "0.1"
scalaVersion := "2.11.7"
libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.2"

 Once that is in place, we can create a JAR package containing the application’s code, then use the spark-submit script to run our program.
$ sbt package
[info] Set current project to Change Logs POC (in build file:/Users/Fab/Repositories/spark/changelogs-poc/)
[info] Compiling 1 Scala source to /Users/Fab/Repositories/spark/changelogs-poc/target/scala-2.10/classes...
[info] Packaging /Users/Fab/Repositories/spark/changelogs-poc/target/scala-2.10/change-logs-poc_2.10-0.1.jar ...
[info] Done packaging.
[success] Total time: 7 s, completed Jan 2, 2016 2:40:38 PM

# Use spark-submit to run your application using 2 cores locally
> $SPARK_HOME/bin/spark-submit   --class "parseCL"   --master local[2]   target/scala-2.11/change-logs-poc_2.11-0.1.jar

