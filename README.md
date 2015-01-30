# uap-clj

A library for extracting browser and operating system information from a raw useragent string:

1. family
2. major number
3. minor number
4. patch level

Implementation is provided for two different classes of use cases: 1.) commandline processing of a textfile containing useragent strings, one per line or 2.) import as User Defined Functions (UDFs) in the Hadoop Hive environment.

## Setup
This project depends on the file `regexes.yaml` actively maintained in the [`ua-parser/uap-core`](https://github.com/ua-parser/uap-core) project, as well
as on the test fixtures `test_ua.yaml` and `test_os.yaml` in the same project. Make sure to run `lein deps` after cloning this code repository, and re-run
on occasion to pull in changes in those `uap-core` assets.

To generate your classes and .jar files:

```bash
lein clean && lein deps && lein compile && lein uberjar
```

###Java and Hive version dependencies

This code has been tested and shown to run under Hive commandline versions 0.12.0 and 0.14.0 when compiled under Java v1.7 (Mac OS X v10.9.5) and run :
```bash
→ java -version
java version "1.7.0_51"
Java(TM) SE Runtime Environment (build 1.7.0_51-b13)
Java HotSpot(TM) 64-Bit Server VM (build 24.51-b03, mixed mode)
```


## Use

### commandline (CLI)

Although this utility was written with the overall goal of providing a small set of Hive UDF functions for extracting browser and O/S data from useragent strings, I've also provided a simple mechanism for creating a basic report outside Hadoop/Hive from the unix commandline:

```bash
/usr/bin/java -jar uap-clj-0.1.0-SNAPSHOT-standalone.jar <input_filename> [<optional_out_filename>]
```

This command takes as its first argument the name of a text file containing one useragent per line, and prints a headerless TSV (tab-separated) file (defaults to `output.tsv`) with this format:

`useragent string<tab>browser family<tab>browser major<tab>browser minor<tab>browser patch<tab>os family<tab>os major<tab>os minor<tab>os patch<newline>`

The resulting file is headerless and can be be trivially imported by your favorite spreadsheet or database ETL tool.

This run option is available as well, particularly useful during development:

```bash
lein run <input_filename> [<optional_out_filename>]
```

Note that the above instructions assume you're using the standalone .jar for development & portability, which will get you running quickly, but it's generally a better thing to use the mininal jarfile which _doesn't_ pull in 50Mb of dependencies (in this case `uap-clj-0.1.0-SNAPSHOT.jar`) and install your dependencies on your classpath.

### Hive UDF

After generating a .jar file - in this case, a standalone uberjar for testing - `scp` to your HDFS client host and copy to a location in HDFS (here, `hdfs:///shared/jars`), then confirm that that `ADD JAR` works in your hive client:

```bash
hive> list jars;
hive> add jar hdfs:///shared/jars/uap-clj-0.1.0-SNAPSHOT-standalone.jar;
converting to local hdfs:///shared/jars/uap-clj-0.1.0-SNAPSHOT-standalone.jar
Added [/tmp/e34eeeef-1af2-4af2-a92d-c2df813deb00_resources/uap-clj-0.1.0-SNAPSHOT-standalone.jar] to class path
Added resources: [hdfs:///shared/jars/uap-clj-0.1.0-SNAPSHOT-standalone.jar]
hive> list jars;
/tmp/e34eeeef-1af2-4af2-a92d-c2df813deb00_resources/uap-clj-0.1.0-SNAPSHOT-standalone.jar
```

Register your functions with names of your choice:

```bash
hive> create temporary function browser as 'uap-clj.udf.Browser';
OK
Time taken: 6.476 seconds
hive> create temporary function os as 'uap-clj.udf.OS';
OK
Time taken: 0.082 seconds
```

If you don't already have a source of useragent strings in form of SELECTable columns of useragent strings in an existing Hive table, you can populate an external table by copying a text file comprising user agent strings, one per line, to a temporary location in HDFS so you can test your newly registered UDFs:

```sql
CREATE EXTERNAL TABLE raw_useragent(agent STRING)
  ROW FORMAT DELIMITED
  LINES TERMINATED BY '\n'
  STORED AS TEXTFILE
  LOCATION '/shared/data/raw/useragent';
```

Note that we've left off a FIELDS TERMINATED BY specification, since each line corresponds to one field. This is the simplest case of a Hive table, EXTERNAL or regular:

```bash
hive> describe raw_useragent;
OK
col_name	data_type	comment
agent               	string
Time taken: 0.735 seconds, Fetched: 1 row(s)
```

Assuming you've moved a source text file (or several) to `hdfs:///shared/data/raw/useragent`, you should see a populated external table, in this case one with 6832 sampled useragent strings for development:

```bash
hive> select count(*) from raw_useragent;
[SNIP SNIP]
Stage-Stage-1: Map: 1  Reduce: 1   Cumulative CPU: 3.52 sec   HDFS Read: 1005886 HDFS Write: 5 SUCCESS
Total MapReduce CPU Time Spent: 3 seconds 520 msec
OK
_c0
6832
Time taken: 50.66 seconds, Fetched: 1 row(s)
```

Now you're ready to play with your data and try out your UDFs:

```bash
hive> SELECT agent, browser(agent) AS browser, os(agent) AS os FROM raw_useragent LIMIT 10;
OK
agent	browser	os
AppleWebKit/531.0 (KHTML, like Gecko) Chrome/1111100111 Safari/531.0	Safari	<empty>	<empty>	<empty>	Other	<empty>	<empty>	<empty>
Chrome/15.0.860.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/15.0.860.0	Chrome	15	0	860	Windows Vista	<empty>	<empty>	<empty>
Iron/2.0.168.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/530.1 (KHTML, like Gecko)	Iron	2	0	168	Windows 7	<empty>	<empty>	<empty>
MMozilla/5.0 (Windows NT 6.0) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30 ChromePlus/1.6.3.0alpha4	Chrome	12	0	742	Windows Vista	<empty>	<empty>	<empty>
Mozilla/1.22 (compatible; MSIE 10.0; Windows 3.1)	IE	10	0	<empty>	Windows 3.1	<empty>	<empty>	<empty>
Mozilla/4.0 (Mozilla/4.0; MSIE 7.0; Windows NT 5.1; FDM; SV1)	IE	7	0	<empty>	Windows XP	<empty>	<empty>	<empty>
Mozilla/4.0 (Mozilla/4.0; MSIE 7.0; Windows NT 5.1; FDM; SV1; .NET CLR 3.0.04506.30)	IE	7	0	<empty>	Windows XP	<empty>	<empty>	<empty>
Mozilla/4.0 (Windows; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)	IE	7	0	<empty>	Windows XP	<empty>	<empty>	<empty>
Mozilla/4.0 (compatible; Crawler; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)	IE	7	0	<empty>	Windows XP	<empty>	<empty>	<empty>
Mozilla/4.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)	IE	10	0	<empty>	Windows 7	<empty>	<empty>	<empty>
Time taken: 0.106 seconds, Fetched: 10 row(s)
```

See that three output columns are specified, but what looks like nine columns are displayed; that's because each of the UDFs - `browser()` and `os()` - output a simple Hive Text object comprising four strings concatenated by non-printing tab characters. To store these values cleanly, you might consider creating a target table containing the outputs of the UDFs split on the embedded tab delimiters stored in columns of Hive `array<string>` type, e.g.:

```bash
hive> CREATE TABLE processed_useragent(
>     agent STRING,
>     browser array<STRING>,
>     os array<STRING>)
>   LOCATION '/shared/data/processed/useragent';
OK
Time taken: 0.315 seconds
hive> describe processed_useragent;
OK
col_name	data_type	comment
agent               	string
browser             	array<string>
os                  	array<string>
Time taken: 0.604 seconds, Fetched: 3 row(s)
```

Now populate this new table from the `raw_useragent` external table:

```bash
hive> INSERT INTO TABLE processed_useragent
>   SELECT agent,
>     split(browser(agent), '\\t'),
>     split(os(agent), '\\t')
>   FROM raw_useragent;
Query ID = hdfs_20150130215656_4903b8b1-00b4-4b2e-a40b-5bb296522d32
Total jobs = 3
[SNIP SNIP]
MapReduce Jobs Launched:
Stage-Stage-1: Map: 1   Cumulative CPU: 16.67 sec   HDFS Read: 1005886 HDFS Write: 1340128 SUCCESS
Total MapReduce CPU Time Spent: 16 seconds 670 msec
OK
agent	_c1	_c2
Time taken: 47.137 seconds

hive> select count(*) as rowcount from processed_useragent;
OK
rowcount
6832
Time taken: 0.123 seconds, Fetched: 1 row(s)
```

Now you have a data source set up for use in useragent analysis:

```bash
hive> select * from processed_useragent limit 10;
OK
processed_useragent.agent	processed_useragent.browser	processed_useragent.os
AppleWebKit/531.0 (KHTML, like Gecko) Chrome/1111100111 Safari/531.0	["Safari","<empty>","<empty>","<empty>"]	["Other","<empty>","<empty>","<empty>"]
Chrome/15.0.860.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/15.0.860.0	["Chrome","15","0","860"]	["Windows Vista","<empty>","<empty>","<empty>"]
Iron/2.0.168.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/530.1 (KHTML, like Gecko)	["Iron","2","0","168"]	["Windows 7","<empty>","<empty>","<empty>"]
MMozilla/5.0 (Windows NT 6.0) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30 ChromePlus/1.6.3.0alpha4	["Chrome","12","0","742"]	["Windows Vista","<empty>","<empty>","<empty>"]
Mozilla/1.22 (compatible; MSIE 10.0; Windows 3.1)	["IE","10","0","<empty>"]	["Windows 3.1","<empty>","<empty>","<empty>"]
Mozilla/4.0 (Mozilla/4.0; MSIE 7.0; Windows NT 5.1; FDM; SV1)	["IE","7","0","<empty>"]	["Windows XP","<empty>","<empty>","<empty>"]
Mozilla/4.0 (Mozilla/4.0; MSIE 7.0; Windows NT 5.1; FDM; SV1; .NET CLR 3.0.04506.30)	["IE","7","0","<empty>"]	["Windows XP","<empty>","<empty>","<empty>"]
Mozilla/4.0 (Windows; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)	["IE","7","0","<empty>"]	["Windows XP","<empty>","<empty>","<empty>"]
Mozilla/4.0 (compatible; Crawler; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)	["IE","7","0","<empty>"]	["Windows XP","<empty>","<empty>","<empty>"]
Mozilla/4.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)	["IE","10","0","<empty>"]	["Windows 7","<empty>","<empty>","<empty>"]
Time taken: 0.095 seconds, Fetched: 10 row(s)
```

From here, you can extract fields of interest in your reports, for example:

```bash
hive> SELECT agent AS logged_agent,
>   browser[0] as browser_family,
>   browser[1] as browser_major,
>   browser[2] as browser_minor,
>   browser[3] as browser_patch,
>   os[0] as os_family,
>   os[1] as os_major,
>   os[2] as os_minor,
>   os[3] as os_patch
> FROM processed_useragent LIMIT 10;
OK
logged_agent	browser_family	browser_major	browser_minor	browser_patch	os_family	os_major	os_minor	os_patch
AppleWebKit/531.0 (KHTML, like Gecko) Chrome/1111100111 Safari/531.0	Safari	<empty>	<empty>	<empty>	Other	<empty>	<empty>	<empty>
Chrome/15.0.860.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/15.0.860.0	Chrome	15	0	860	Windows Vista	<empty>	<empty>	<empty>
Iron/2.0.168.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/530.1 (KHTML, like Gecko)	Iron	2	0	168	Windows 7	<empty>	<empty>	<empty>
MMozilla/5.0 (Windows NT 6.0) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30 ChromePlus/1.6.3.0alpha4	Chrome	12	0	742	Windows Vista	<empty>	<empty>	<empty>
Mozilla/1.22 (compatible; MSIE 10.0; Windows 3.1)	IE	10	0	<empty>	Windows 3.1	<empty>	<empty>	<empty>
Mozilla/4.0 (Mozilla/4.0; MSIE 7.0; Windows NT 5.1; FDM; SV1)	IE	7	0	<empty>	Windows XP	<empty>	<empty>	<empty>
Mozilla/4.0 (Mozilla/4.0; MSIE 7.0; Windows NT 5.1; FDM; SV1; .NET CLR 3.0.04506.30)	IE	7	0	<empty>	Windows XP	<empty>	<empty>	<empty>
Mozilla/4.0 (Windows; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)	IE	7	0	<empty>	Windows XP	<empty>	<empty>	<empty>
Mozilla/4.0 (compatible; Crawler; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)	IE	7	0	<empty>	Windows XP	<empty>	<empty>	<empty>
Mozilla/4.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)	IE	10	0	<empty>	Windows 7	<empty>	<empty>	<empty>
Time taken: 0.131 seconds, Fetched: 10 row(s)
```

The foregoing example is not intended for production, and is merely an example of a toy use case.

## Future / Enhancements

What's up next:
1. Implement Device parsing;
2. Re-implement UDFs as GenericUDFs;
3. Refine Browser and OS parsing to catch edge cases;
4. Write a preprocessor for the `speclj` testrunner which filters against text fixtures with no corresponding `regexes.yaml` entry.

Pull requests will be very happily considered.  

__Maintained by Russell Whitaker__
