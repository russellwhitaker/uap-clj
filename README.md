# uap-clj

A [`ua-parser/uap-core`](https://github.com/ua-parser/uap-core) based Clojure library for extracting browser, operating system, and device information from a raw useragent string.

This library is also used by an Apache Hadoop Hive Simple UDF, [`uap-clj-hiveudf`](https://github.com/russellwhitaker/uap-clj-hiveudf), and an Amazon AWS Lambda function, [`uap-clj-lambda`](https://github.com/russellwhitaker/uap-clj-lambda).

For Java programmers, an API is provided as well, allowing direct use in native Java code (see below.)

The canonical version of this project lives at [`russellwhitaker/uap-clj`](https://github.com/russellwhitaker/uap-clj) and is mirrored at [`ua-parser/uap-clj`](https://github.com/ua-parser/uap-clj).

## Setup

Add this to the `:dependencies` stanza of your `project.clj`:

[![Clojars Project](http://clojars.org/uap-clj/latest-version.svg)](http://clojars.org/uap-clj)

`uap-clj` depends on the file `regexes.yaml` actively maintained in the public [`ua-parser/uap-core`](https://github.com/ua-parser/uap-core) repository,
as well as on the test fixtures `test_ua.yaml`, `test_os.yaml`, and `test_device.yaml` contained therein. Be sure to run `lein deps` after cloning this code repository, and re-run on occasion to pull in changes committed to those `uap-core` assets.

To generate your classes and .jar files:

```bash
→ lein clean && lein uberjar
Compiling uap-clj.browser
Compiling uap-clj.common
Compiling uap-clj.core
Compiling uap-clj.device
Compiling uap-clj.java.api.browser
Compiling uap-clj.java.api.device
Compiling uap-clj.java.api.os
Compiling uap-clj.os
Created /Users/<username>/dev/uap-clj/target/uap-clj-1.1.1.jar
Created /Users/<username>/dev/uap-clj/target/uap-clj-1.1.1-standalone.jar
```

### Java dependencies

This code has been tested and shown to run under Java v1.7 and v1.8:

```bash
→ java -version
java version "1.7.0_51"
Java(TM) SE Runtime Environment (build 1.7.0_51-b13)
Java HotSpot(TM) 64-Bit Server VM (build 24.51-b03, mixed mode)

→ java -version
java version "1.8.0_66"
Java(TM) SE Runtime Environment (build 1.8.0_66-b17)
Java HotSpot(TM) 64-Bit Server VM (build 25.66-b17, mixed mode)
```

## Development
### Running the test suite

This project uses [`speclj`](http://speclj.com). The core test suite comprises almost entirely test generators built from reading in test fixtures from the [`ua-parser/uap-core`](https://github.com/ua-parser/uap-core) repository, which themselves are pulled into the local workspace as dependencies using [`tobyhede/lein-git-deps`](https://github.com/tobyhede/lein-git-deps).

```bash
→ lein spec --reporter=c

Ran 107336 tests containing 107336 assertions.
0 failures, 0 errors.
```
The test suite runs against all the browser, o/s, and device YAML fixtures in [`ua-parser/uap-core/tests`](https://github.com/ua-parser/uap-core/blob/master/tests), for both the native Clojure core library and the Java API.

## Use

### Commandline (CLI)

```bash
/usr/bin/java -jar uap-clj-1.1.1-standalone.jar <input_filename> [<optional_out_filename>]
```

This command takes as its first argument the name of a text file containing one useragent per line, and prints a TSV (tab-separated) file - defaulting to `useragent_lookup.tsv` - with this line format:

`useragent string<tab>browser family<tab>browser major<tab>browser minor<tab>browser patch<tab>os family<tab>os major<tab>os minor<tab>os patch<tab>os patch minor<tab>device family<tab>device brand<tab>device model<newline>`

The output file has a single-line header and can be be trivially imported by your favorite spreadsheet or database ETL tool.

A Leiningen-based run option is available as well, which is particularly convenient during development:

```bash
lein run <input_filename> [<optional_out_filename>]
```

Note that these instructions assume you're using the standalone version of the project .jar file, for development & portability: this will get you running quickly, but it's almost always a better thing to use the mininal jarfile instead, since it _doesn't_ pull in 4Mb of dependencies. To enable this, you'll need to install prerequisite dependencies (specified in `project.clj`) on your classpath.

### In a Clojure REPL

If you'd like to explore useragent data interactively, and you have Leiningen installed, you can do this:

```clojure
→ lein repl
nREPL server started on port 52739 on host 127.0.0.1 - nrepl://127.0.0.1:52739
REPL-y 0.3.7, nREPL 0.2.10
Clojure 1.7.0
Java HotSpot(TM) 64-Bit Server VM 1.8.0_66-b17
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)
 Results: Stored in vars *1, *2, *3, an exception in *e

uap-clj.core=> (def my-useragent "Lenovo-A288t_TD/S100 Linux/2.6.35 Android/2.3.5 Release/02.29.2012 Browser/AppleWebkit533.1 Mobile Safari/533.1 FlyFlow/1.4")
#'uap-clj.core/my-useragent

uap-clj.core=> (pprint (lookup-useragent my-useragent))
{:ua
 "Lenovo-A288t_TD/S100 Linux/2.6.35 Android/2.3.5 Release/02.29.2012 Browser/AppleWebkit533.1 Mobile Safari/533.1 FlyFlow/1.4",
 :browser
 {:family "Baidu Explorer", :major "1", :minor "4", :patch ""},
 :os
 {:family "Android",
  :major "2",
  :minor "3",
  :patch "5",
  :patch_minor ""},
 :device
 {:family "Lenovo A288t_TD", :brand "Lenovo", :model "A288t_TD"}}
nil

uap-clj.core=> (pprint (lookup-useragent "Some crazy useragent we've not yet categorized/v0.2.0/yomama@yamama.co.jp"))
{:ua
 "Some crazy useragent we've not yet categorized/v0.2.0/yomama@yamama.co.jp",
 :browser {:family "Other", :patch nil, :major nil, :minor nil},
 :os
 {:patch_minor nil,
  :family "Other",
  :patch nil,
  :major nil,
  :minor nil},
 :device {:family "Other", :brand nil, :model nil}}
nil

uap-clj.core=>
```
You can also use any other Clojure REPL for the same type of interactive data exploration.

### In a RESTful API

If you have an Heroku account, [you can easily deploy a Compojure app there](https://devcenter.heroku.com/articles/getting-started-with-clojure) using GET and POST
routes that look something like this:

```clojure
(defroutes app
  (GET "/ua" {{input :input} :params}
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body (pr-str (lookup-useragent input))})
  (POST "/" {{ua :ua} :params}
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body (pr-str (lookup-useragent ua))})
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))
```
All you need to enable the use of the `lookup-useragent` function here is to add
`[uap-clj "1.1.1"]` to the `:dependencies` vector in your Compojure app's `project.clj`,
and `[uap-clj.core :refer [lookup-useragent]]` to the `:require` vector of your `web.clj`.
Then you can do this type of thing after deployment:

```bash
→ curl --data "ua=AppleCoreMedia/1.0.0.12F69 (Apple TV; U; CPU OS 8_3 like Mac OS X; en_us)" http://<your_app>.herokuapp.com
{:ua "AppleCoreMedia/1.0.0.12F69 (Apple TV; U; CPU OS 8_3 like Mac OS X; en_us)", :browser {:family "Other", :patch nil, :major nil, :minor nil}, :os {:family "ATV OS X", :major "", :minor "", :patch "", :patch_minor ""}, :device {:family "AppleTV", :brand "Apple", :model "AppleTV"}}
```

### In native Java projects

Add this repository to your `pom.xml`:

```xml
<repository>
  <id>clojars</id>
  <url>http://clojars.org/repo/</url>
</repository>
```

Then add these dependencies to your `pom.xml`:

```xml
<dependency>
  <groupId>org.clojure</groupId>
  <artifactId>clojure</artifactId>
  <version>1.7.0</version>
</dependency>
<dependency>
  <groupId>uap-clj</groupId>
  <artifactId>uap-clj</artifactId>
  <version>1.1.1</version>
</dependency>
```

Example usage:

```java
package useragent;

import java.util.HashMap;

// Java API for uap-clj Clojure implementation of useragent parser
import uap_clj.java.api.*;

public class Parser {
  public Parser() {};

  public static void main(String[] args) {
    String ua = args[0];

    HashMap b = Browser.lookup(ua);
    HashMap o = OS.lookup(ua);
    HashMap d = Device.lookup(ua);

    System.out.println("Browser family: " + b.get("family"));
    System.out.println("Browser major number: " + b.get("major"));
    System.out.println("Browser minor number: " + b.get("minor"));
    System.out.println("Browser patch number: " + b.get("patch"));

    System.out.println("O/S family: " + o.get("family"));
    System.out.println("O/S major number: " + o.get("major"));
    System.out.println("O/S minor number: " + o.get("minor"));
    System.out.println("O/S patch number: " + o.get("patch"));
    System.out.println("O/S patch_minor number: " + o.get("patch_minor"));

    System.out.println("Device family: " + d.get("family"));
    System.out.println("Device brand: " + d.get("brand"));
    System.out.println("Device model: " + d.get("model"));
  }
}
```

```bash
→ mvn compile
→ mvn exec:java -Dexec.mainClass="useragent.Parser" -Dexec.args="'Lenovo-A288t_TD/S100 Linux/2.6.35 Android/2.3.5 Release/02.29.2012 Browser/AppleWebkit533.1 Mobile Safari/533.1 FlyFlow/1.4'"
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building UapJavaWrapper 1.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- exec-maven-plugin:1.4.0:java (default-cli) @ UapJavaWrapper ---
Browser family: Baidu Explorer
Browser major number: 1
Browser minor number: 4
Browser patch number:
O/S family: Android
O/S major number: 2
O/S minor number: 3
O/S patch number: 5
O/S patch_minor number:
Device family: Lenovo A288t_TD
Device brand: Lenovo
Device model: A288t_TD
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1.961 s
[INFO] Finished at: 2015-11-03T14:39:45-08:00
[INFO] Final Memory: 15M/301M
[INFO] ------------------------------------------------------------------------
```

## Future / Enhancements

Next up: break out browser, o/s, and device core native functions for separate execution.

I respond to issues filed and will happily consider pull requests.

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
