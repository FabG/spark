// Simple Scala proof of concept to parse our Change Logs file
// Author: Fabrice Guillaume
// Date: Jan 02 2016

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
 
object parseCL {
  // case class needs to be defined outside of the method needing it 
  // or it will fail on the ".toDF().registerTempTable compilation with SBT - although this works in spark-shell...
  case class ContactInfo(cb_user_id: String, cb_contact_id: String, change_type: Short, record_type: Short, 
    source: Short, raw_col_1: String, raw_col_2: String)

  def main(args: Array[String]) {
    // Spark Conf and Spark Core Context
    val conf = new SparkConf().setAppName("Change Logs Parser POC")
    val sc = new SparkContext(conf)

    // Spark SQL Context
    val sqlContext= new org.apache.spark.sql.SQLContext(sc)
    // Import implicits *** to keep here AFTER the sqlContect is created - don't move in header
    import sqlContext.implicits._


    // Input file (ChangeLogs TSV file)
    val InputFile = "./input/users.contact-changes.1000-users-sample100.tsv"

    // Input Data RDD (Resilient Distributed Dataset)
    val inputData = sc.textFile(InputFile).map(_.split("\t"))

    // Transformation #1: filter rows based Keep Record types of interest
    // 1=Name, 2=Address, 3=Email, 6=Phone Number, 13=Job Info
    // Creating this RDD parallelizing the collection of record types
    val filteredRecTypeData = inputData.filter(line => 
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

    println("Out of the following original contacts: %s, After filtering based on Record Types 1,2,3,6,13, we now have: %s".format(inputData.count(), filteredRecTypeData.count()))
    println("Lines with name: %s, Lines with email: %s, Lines with phone: %s, Lines with JobInfo: %s, Lines with Address: %s".format(numNames, numEmails, numPhones, numJobinfo, numAddress))



    // Now selectedColumns has the following columns:
    // We will use SparkSQL to do so - the schema will be defined using a case class
    //  The names of the arguments to the case class are read using reflection and become the names of the columns
    // 0=cb_contact_id, 1=cb_user_id, 2=record_type, 3=cb_timestamp
    // 5=created_on, 6=modified_on, 8=change_type, 9=item_type
    // 10=source, 12=raw_col_1(All,FN,Org,Street1), 13=raw_col_2(LN,Title,Street2)
    // 14=raw_col_3(City), 15=raw_col_4(Zip), 16=raw_col_5(State)
    // 17=raw_col_6(Country)
    // Plus some normalized fields:
    // 23=(All,FN,Org,Street1), 24=(LN,Title,Street2), 25=Phone, 26=PhoneExtension
    // 27=first6 Digits Phone, 28=Location

    // RDD to filter columns - not used as I will filter those columns in the SQL RDD instead
    // val indicesColumns = Array(0,1,2,3,5,6,8,9,10,12,13,14,15,16,17)
    // val selectedColumns = filteredRecTypeData.map(array => indicesColumns.map(array))

    // contact Table: 7 columns
    //   cb_user_id|cb_contact_id|change_type|record_type|source|raw_col_1|raw_col_2
    // case class ContactInfo(cb_user_id: String, cb_contact_id: String, change_type: Short, record_type: Short, 
    //   source: Short, raw_col_1: String, raw_col_2: String)

    // Create an RDD of Person objects and register it as a table
    val contact = filteredRecTypeData.map(p => ContactInfo(p(1),p(0),p(8).trim.toShort,p(2).trim.toShort,
      p(10).trim.toShort,p(12),p(13)))

    // Note: a DataFrame is equivalent to a relational table in Spark SQL
    contact.toDF().registerTempTable("contact")

    // Now run SQL queries against our newly created Temp Table
    val userContactFirstNames = sqlContext.sql("SELECT cb_user_id, cb_contact_id, change_type, raw_col_1 as FirstName, count(*) as Count from contact where record_type = 1 group by cb_user_id, cb_contact_id, change_type, raw_col_1")
    val userContactLastNames = sqlContext.sql("SELECT cb_user_id, cb_contact_id, change_type, raw_col_2 as LastName, count(*) as Count from contact where record_type = 1 group by cb_user_id, cb_contact_id, change_type, raw_col_2")
    val userContactPhones = sqlContext.sql("SELECT cb_user_id, cb_contact_id, change_type, raw_col_1 as Phone, count(*) as Count from contact where record_type = 6 group by cb_user_id, cb_contact_id, change_type, raw_col_1")
    val userContactEmails = sqlContext.sql("SELECT cb_user_id, cb_contact_id, change_type, raw_col_1 as Email, count(*) as Count from contact where record_type = 3 group by cb_user_id, cb_contact_id, change_type, raw_col_1")
    val userContactCompanies = sqlContext.sql("SELECT cb_user_id, cb_contact_id, change_type, raw_col_1 as Company, count(*) as Count from contact where record_type = 13 group by cb_user_id, cb_contact_id, change_type, raw_col_1")
    val userContactTitles = sqlContext.sql("SELECT cb_user_id, cb_contact_id, change_type, raw_col_2 as Title, count(*) as Count from contact where record_type = 13 group by cb_user_id, cb_contact_id, change_type, raw_col_2")

    println("Results from SparkSQL:")
    println(" - First Names")
    userContactFirstNames.collect().foreach(println)
    println(" - Last Names")
    userContactLastNames.collect().foreach(println)
    println(" - Phones")
    userContactPhones.collect().foreach(println)
    println(" - Emails")
    userContactEmails.collect().foreach(println)
    println(" - Companies")
    userContactCompanies.collect().foreach(println)
    println(" - Titles")
    userContactTitles.collect().foreach(println)

    // Output File
    val outputPath = "./output"

    // Export to CSV file the filtered lines using the Ddatabricks spark-csv library
    // userContactSummary.saveAsTextFile(outputPath)
    //userContactSummary.write.format("com.databricks.spark.csv").save(outputPath)
   
  }
}
