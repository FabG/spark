/* Simple Scala proof of concept to parse our Change Logs file */
/* Author: Fabrice Guillaume */
/* Date: Jan 02 2016 */

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import java.text.SimpleDateFormat
import scala.util.{Try, Success, Failure}
 
object parseCL {
  def main(args: Array[String]) {
    /* Spark Conf and Spark Context */
    val conf = new SparkConf().setAppName("Change Logs Parser POC")
    val sc = new SparkContext(conf)

    /* Input file (ChangeLogs TSV file) */
    val InputFile = "./input/users.contact-changes.1000-users-sample100.tsv"

    /* Input Data RDD (Resilient Distributed Dataset) */
    val inputData = sc.textFile(InputFile)

    /* Output File */
    val outputFile = "./output/parsed.contact-changes_sample10.tsv"

    /* Keep only the Record types of interest */
    /* 1=Name, 2=Address, 3=Email, 6=Phone Number, 13=Job Info */
    /* Creating this RDD parallelizing the collection of record types */
    val splitInputData = inputData.map(_.split("\t"))
    val filteredRecTypeData = splitInputData.filter(line => 
      line(2).equals("1") ||
      line(2).equals("2") ||
      line(2).equals("3") ||
      line(2).equals("6") ||
      line(2).equals("13") )
  
    val numNames = filteredRecTypeData.filter(line => line(2).equals("1")).count()
    val numEmails = filteredRecTypeData.filter(line => line(2).equals("3")).count()
    val numPhones = filteredRecTypeData.filter(line => line(2).equals("6")).count()
    val numJobinfo = filteredRecTypeData.filter(line => line(2).equals("13")).count()
    val numAddress = filteredRecTypeData.filter(line => line(2).equals("2")).count()

    println("Out of the following original contacts: %s, After filtering based on Record Types 1,2,3,6,13, we now have: %s".format(splitInputData.count(), filteredRecTypeData.count()))
    println("Lines with name: %s, Lines with email: %s, Lines with phone: %s, Lines with JobInfo: %s, Lines with Address: %s".format(numNames, numEmails, numPhones, numJobinfo, numAddress))

    /* Actions */

   /* Export to file the filtered lines */
   filteredRecTypeData.saveAsTextFile(outputFile)



    /* Transformation: only keep fields of value */
    /* 0=cb_contact_id, 1=cb_user_id, 2=cb_user_id, 3=cb_timestamp */
    /* 5=created_on, 6=modified_on, 8=change_type, 9=item_type */
    /* 10=source, 12=raw_col_1(All,FN,Org,Street1), 13=raw_col_2(LN,Title,Street2) */
    /* 14=raw_col_3(City), 15=raw_col_4(Zip), 16=raw_col_5(State) */
    /* 17=raw_col_6(Country) */
    /* Plus some normalized fields: */
    /* 23=(All,FN,Org,Street1), 24=(LN,Title,Street2), 25=Phone, 26=PhoneExtension */
    /* 27=first6 Digits Phone, 28=Location */


   
  }
}
