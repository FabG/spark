/* Simple Scala proof of concept to parse our Change Logs file */
/* Author: Fabrice Guillaume */
/* Date: Jan 02 2016 */

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object parseCL {
  def main(args: Array[String]) {
    /* Input file (ChangeLogs TSV file) */
    val changeLogsFile = "./input/users.contact-changes.1000-users_sample10.tsv"

    val outputFile = "./output/parsed.contact-changes_sample10.tsv"

    /* Spark Conf and Spark Context */
    val conf = new SparkConf().setAppName("Change Logs Parser POC")
    val sc = new SparkContext(conf)
    /* cache RDD  */
    val changeLogsData = sc.textFile(changeLogsFile, 2).cache()

    /* Transformations */
    val changeLogsParse = changeLogsData.map(line => line.split("\t"))

    /* Filter source = 600 */
    val filteredSource = changeLogsParse.filter(line => line(10).equals("600"))

    /* Keep only the Record types of interest */
    /* 1=Name, 2=Address, 3=Email, 6=Phone Number, 13=Job Info */
    /* RecordType is 3rd in our TSV file (2nd in our RDD as it's an array) */
    
    val numNames = filteredSource.filter(line => line(2).equals("1")).count()
    val numEmails = filteredSource.filter(line => line(2).equals("3")).count()
    val numPhones = filteredSource.filter(line => line(2).equals("6")).count()
    val numJobinfo = filteredSource.filter(line => line(2).equals("13")).count()
    val numAddress = filteredSource.filter(line => line(2).equals("2")).count()

    /* Actions */
    println("Lines with name: %s, Lines with email: %s, Lines with phone: %s, Lines with JobInfo: %s, Lines with Address: %s".format(numNames, numEmails, numPhones, numJobinfo, numAddress))

   /* Export to file the filtered lines */
   filteredSource.saveAsTextFile(outputFile)

   
  }
}
