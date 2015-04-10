# uap-clj

A [`ua-parser/uap-core`](https://github.com/ua-parser/uap-core) based library for extracting browser and operating system information from a raw useragent string:

1. family
2. major number
3. minor number
4. patch level

This library is also used by an Apache Hadoop Hive Simple UDF, [`uap-clj-hiveudf`](https://github.com/russellwhitaker/uap-clj-hiveudf).

## Setup

Add this to the `:dependencies` stanza of your `project.clj`:

[![Clojars Project](http://clojars.org/uap-clj/latest-version.svg)](http://clojars.org/uap-clj)

`uap-clj` depends on the file `regexes.yaml` actively maintained in the public [`ua-parser/uap-core`](https://github.com/ua-parser/uap-core) repository, as well as on the test fixtures `test_ua.yaml` and `test_os.yaml` contained therein. Be sure to run `lein deps` after cloning this code repository, and re-run on occasion to pull in changes committed to those `uap-core` assets.

To generate your classes and .jar files:

```bash
lein clean && lein uberjar
```

###Java dependencies

This code has been tested and shown to run under Java v1.7 (Mac OS X v10.9.5):

```bash
→ java -version
java version "1.7.0_51"
Java(TM) SE Runtime Environment (build 1.7.0_51-b13)
Java HotSpot(TM) 64-Bit Server VM (build 24.51-b03, mixed mode)
```

## Use

### commandline (CLI)


```bash
/usr/bin/java -jar uap-clj-0.3.0-standalone.jar <input_filename> [<optional_out_filename>]
```

This command takes as its first argument the name of a text file containing one useragent per line, and prints a TSV (tab-separated) file - defaulting to `output.tsv` - with this line format:

`useragent string<tab>browser family<tab>browser major<tab>browser minor<tab>browser patch<tab>os family<tab>os major<tab>os minor<tab>os patch<newline>`

The output file has a single-line header and can be be trivially imported by your favorite spreadsheet or database ETL tool.

A Leiningen-based run option is available as well, which is particularly convenient during development:

```bash
lein run <input_filename> [<optional_out_filename>]
```

Note that these instructions assume you're using the standalone version of the project .jar file, for development & portability: this will get you running quickly, but it's almost always a better thing to use the mininal jarfile instead, since it _doesn't_ pull in 4Mb of dependencies. To enable this, you'll need to install prerequisite dependencies (specified in `project.clj`) on your classpath.


## Future / Enhancements

What's up next:

1. Implement Device parsing;
2. Refine Browser and OS parsing to deal with some apparently hairy regex substitution not accounted for in the initial Implementation;
3. Write a preprocessor for the `speclj` testrunner which filters against text fixtures with no corresponding `regexes.yaml` entry (the need for which will become apparent to the user who runs `lein spec --reporter=d`.)

Pull requests will be very happily considered.

__Maintained by Russell Whitaker__

## License

The MIT License (MIT)

Copyright (c) 2015 Russell Whitaker

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
