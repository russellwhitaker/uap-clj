# uap-clj

[![CircleCI](https://circleci.com/gh/russellwhitaker/uap-clj.svg?style=svg)](https://circleci.com/gh/russellwhitaker/uap-clj)

A [`ua-parser/uap-core`](https://github.com/ua-parser/uap-core) based Clojure library for extracting browser, operating system, and device information from a raw useragent string.

This library is also used by an Apache Hadoop Hive Simple UDF, [`uap-clj-hiveudf`](https://github.com/russellwhitaker/uap-clj-hiveudf), and an Amazon AWS Lambda function, [`uap-clj-lambda`](https://github.com/russellwhitaker/uap-clj-lambda).

For Java programmers, an API is provided as well, allowing direct use in native Java code (see below.)

The canonical version of this project lives at [`russellwhitaker/uap-clj`](https://github.com/russellwhitaker/uap-clj) and is mirrored at [`ua-parser/uap-clj`](https://github.com/ua-parser/uap-clj).

## Setup

Add this to the `:dependencies` stanza of your `project.clj`:

[![Clojars Project](http://clojars.org/uap-clj/latest-version.svg)](http://clojars.org/uap-clj)

`uap-clj` depends on the file `regexes.yaml` actively maintained in the public [`ua-parser/uap-core`](https://github.com/ua-parser/uap-core) repository, as well as on the test fixtures `test_ua.yaml`, `test_os.yaml`, and `test_device.yaml` contained therein. Be sure to run `rm -rf .lein-get-deps && lein git-deps && lein deps` after cloning this code repository, and re-run on occasion to pull in changes committed to those `uap-core` assets.

`uap-clj` needs `regexes.yaml` in `edn` format. One way do this conversion is to run:

``` bash
uap-clj$ cat src/resources/submodules/regexes.yaml | jet -i yaml -o edn -k > resources/regexes.edn
```

Install [jet](https://github.com/borkdude/jet) CLI:

``` bash

brew install borkdude/brew/jet
```

Converting the regexes.yaml file to native Clojure EDN data format removes the runtime clj-yaml dependency.

To generate your classes and .jar files:

```bash
→ lein build
Compiling uap-clj.browser
Compiling uap-clj.common
Compiling uap-clj.core
Compiling uap-clj.device
Compiling uap-clj.java.api.browser
Compiling uap-clj.java.api.device
Compiling uap-clj.java.api.os
Compiling uap-clj.os
Created /Users/whitaker/dev/ua-parser/uap-clj/target/uap-clj-1.7.0.jar
Created /Users/whitaker/dev/ua-parser/uap-clj/target/uap-clj-1.7.0-standalone.jars
```

### Java dependencies

This code was originally tested and shown to run under Java 7, and hasn't broken yet through Java 16:

```bash
→ java -version
openjdk version "17.0.7" 2023-04-18
OpenJDK Runtime Environment Temurin-17.0.7+7 (build 17.0.7+7)
OpenJDK 64-Bit Server VM Temurin-17.0.7+7 (build 17.0.7+7, mixed mode, sharing)s
```

## Development
### Running the test suite

This project uses [`speclj`](http://speclj.com). The core test suite comprises almost entirely test generators built from reading in test fixtures from the [`ua-parser/uap-core`](https://github.com/ua-parser/uap-core) repository, which themselves are pulled into the local workspace as dependencies using [`tobyhede/lein-git-deps`](https://github.com/tobyhede/lein-git-deps).

```bash
→ lein test

Finished in 0.26248 seconds
112663 examples, 0 failuress
```
The test suite runs against all the browser, o/s, and device YAML fixtures in [`ua-parser/uap-core/tests`](https://github.com/ua-parser/uap-core/blob/master/tests), for both the native Clojure core library and the Java API. The `clojars` artifact is compiled with these fixtures linked as a git submodule dependency.

## Use / Production

The basic utility functions of this library comprise `useragent`, `browser`, `os`, and `device`; memoized versions of the same functions are provided as conveniences for production environments where a requirement for low latency response from large numbers of rapidly repeated requests is worth the memory penalty incurred by the need to maintain a growing cache of input/output value mappings. Accordingly, these functions are named `useragent-memoized`, `browser-memoized`, `os-memoized`, and `device-memoized`.

### Commandline (CLI)

```bash
/usr/bin/java -jar uap-clj-1.7.0-standalone.jar <input_filename> [<optional_out_filename>]
```

This command takes as its first argument the name of a text file containing one useragent per line, and prints a TSV (tab-separated) file - defaulting to `useragent_lookup.tsv` - with this line format:

`useragent string<tab>browser family<tab>browser major<tab>browser minor<tab>browser patch<tab>os family<tab>os major<tab>os minor<tab>os patch<tab>os patch_minor<tab>device family<tab>device brand<tab>device model<newline>`

The output file has a single-line header and can be be trivially imported by your favorite spreadsheet or database ETL tool.

A Leiningen-based run option is available as well, which is particularly convenient during development:

```bash
lein run <input_filename> [<optional_out_filename>]
```

Note that these instructions assume you're using the standalone version of the project .jar file, for development & portability: this will get you running quickly, but it's almost always a better thing to use the mininal jarfile instead, since it _doesn't_ pull in 4Mb of dependencies. To enable this, you'll need to install prerequisite dependencies (specified in `project.clj`) on your classpath.

### In a Clojure REPL

If you'd like to explore useragent data interactively, and you have Leiningen installed, you can do this:

```clojure
$ lein repl
nREPL server started on port 54100 on host 127.0.0.1 - nrepl://127.0.0.1:54100
REPL-y 0.4.4, nREPL 0.8.3
Clojure 1.11.1
OpenJDK 64-Bit Server VM 17.0.7+7
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)
 Results: Stored in vars *1, *2, *3, an exception in *e

my-project.core=> (require `[uap-clj.core :as u])
nil
my-project.core=> (def my-useragent "Lenovo-A288t_TD/S100 Linux/2.6.35 Android/2.3.5 Release/02.29.2012 Browser/AppleWebkit533.1 Mobile Safari/533.1 FlyFlow/1.4")
#'my-project.core/my-useragent
my-project.core=> (pprint (u/useragent my-useragent))
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
```

If you only care about particular `browser`, `os`, or `device` values, you can look each of these separately:

```clojure
uap-clj.core=> (browser my-useragent)
{:family "Baidu Explorer", :major "1", :minor "4", :patch ""}
uap-clj.core=> (os my-useragent)
{:family "Android", :major "2", :minor "3", :patch "5", :patch_minor ""}
uap-clj.core=> (device my-useragent)
{:family "Lenovo A288t_TD", :brand "Lenovo", :model "A288t_TD"}
```

Unknown useragents are classified "Other":

```clojure
uap-clj.core=> (pprint (useragent "Some unknown useragent we've not yet categorized/v0.2.0/yomama@yamama.co.jp"))
{:ua
 "Some unknown useragent we've not yet categorized/v0.2.0/yomama@yamama.co.jp",
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

If you have an Heroku account, [you can easily deploy a Compojure app there](https://devcenter.heroku.com/articles/getting-started-with-clojure) using GET and POST routes that look something like this:

```clojure
(defroutes app
  (GET "/ua" {{input :input} :params}
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body (pr-str (useragent input))})
  (POST "/" {{ua :ua} :params}
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body (pr-str (useragent ua))})
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))
```
All you need to enable the use of the `lookup-useragent` function here is to add `[uap-clj "1.7.0"]` to the `:dependencies` vector in your Compojure app's `project.clj` (or a similar entry to `deps.edn` if you're using a more modern `tools-deps` toolchain), and `[uap-clj.core :refer [lookup-useragent]]` to the `:require` vector of your `web.clj`. Then you can do this type of thing after deployment:

```bash
→ curl --data "ua=AppleCoreMedia/1.0.0.12F69 (Apple TV; U; CPU OS 8_3 like Mac OS X; en_us)" http://<your_app>.herokuapp.com {:ua "AppleCoreMedia/1.0.0.12F69 (Apple TV; U; CPU OS 8_3 like Mac OS X; en_us)", :browser {:family "Other", :patch nil, :major nil, :minor nil}, :os {:family "ATV OS X", :major "", :minor "", :patch "", :patch_minor ""}, :device {:family "AppleTV", :brand "Apple", :model "AppleTV"}}
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
  <version>1.11.1</version>
</dependency>
<dependency>
  <groupId>uap-clj</groupId>
  <artifactId>uap-clj</artifactId>
  <version>1.7.0</version>
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
[INFO] --- exec-maven-plugin:1.7.0:java (default-cli) @ UapJavaWrapper ---
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
[INFO] Finished at: 2023-04-10T14:39:45-08:00
[INFO] Final Memory: 15M/301M
[INFO] ------------------------------------------------------------------------
```

## Future / Enhancements

* add option to source `regexes.yaml` from an S3 bucket
* replace `speclj` with `clojure.test`
* add `clojure.spec`

__Maintained by Russell Whitaker__

## License

The MIT License (MIT)

Copyright (c) 2015-2023 Russell Whitaker

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
