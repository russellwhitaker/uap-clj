# uap-clj

A [`ua-parser/uap-core`](https://github.com/ua-parser/uap-core) based library for extracting browser, operating system, and device information from a raw useragent string.

This library is also used by an Apache Hadoop Hive Simple UDF, [`uap-clj-hiveudf`](https://github.com/russellwhitaker/uap-clj-hiveudf).

## Setup

Add this to the `:dependencies` stanza of your `project.clj`:

[![Clojars Project](http://clojars.org/uap-clj/latest-version.svg)](http://clojars.org/uap-clj)

`uap-clj` depends on the file `regexes.yaml` actively maintained in the public [`ua-parser/uap-core`](https://github.com/ua-parser/uap-core) repository,
as well as on the test fixtures `test_ua.yaml`, `test_os.yaml`, and `test_device.yaml` contained therein. Be sure to run `lein deps` after cloning this code repository, and re-run on occasion to pull in changes committed to those `uap-core` assets.

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
## Development
### Running the test suite

This project uses [`speclj`](http://speclj.com). The core test suite comprises almost entirely test generators built from reading in test fixtures from the [`ua-parser/uap-core`](https://github.com/ua-parser/uap-core) repository, which themselves are pulled into the local workspace as dependencies using [`tobyhede/lein-git-deps`](https://github.com/tobyhede/lein-git-deps).

```bash
→ lein spec --reporter=c

Ran 53668 tests containing 53668 assertions.
0 failures, 0 errors.
```
The test suite runs against all the browser, o/s, and device YAML fixtures in [`ua-parser/uap-core/tests`](https://github.com/ua-parser/uap-core/blob/master/tests).

## Use

### commandline (CLI)

```bash
/usr/bin/java -jar uap-clj-1.0.3-standalone.jar <input_filename> [<optional_out_filename>]
```

This command takes as its first argument the name of a text file containing one useragent per line, and prints a TSV (tab-separated) file - defaulting to `useragent_lookup.tsv` - with this line format:

`useragent string<tab>browser family<tab>browser major<tab>browser minor<tab>browser patch<tab>os family<tab>os major<tab>os minor<tab>os patch<tab>os patch minor<tab>device family<tab>device brand<tab>device model<newline>`

The output file has a single-line header and can be be trivially imported by your favorite spreadsheet or database ETL tool.

A Leiningen-based run option is available as well, which is particularly convenient during development:

```bash
lein run <input_filename> [<optional_out_filename>]
```

Note that these instructions assume you're using the standalone version of the project .jar file, for development & portability: this will get you running quickly, but it's almost always a better thing to use the mininal jarfile instead, since it _doesn't_ pull in 4Mb of dependencies. To enable this, you'll need to install prerequisite dependencies (specified in `project.clj`) on your classpath.

### REPL

If you'd like to explore useragent data interactively, and you have Leiningen installed, you can do this:

```clojure
→ lein repl
nREPL server started on port 57950 on host 127.0.0.1 - nrepl://127.0.0.1:57950
REPL-y 0.3.7, nREPL 0.2.10
Clojure 1.7.0
Java HotSpot(TM) 64-Bit Server VM 1.7.0_51-b13
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
`[uap-clj "1.0.3"]` to the `:dependencies` vector in your Compojure app's `project.clj`,
and `[uap-clj.core :refer [lookup-useragent]]` to the `:require` vector of your `web.clj`.
Then you can do this type of thing after deployment:

```bash
→ curl --data "ua=AppleCoreMedia/1.0.0.12F69 (Apple TV; U; CPU OS 8_3 like Mac OS X; en_us)" http://<your_app>.herokuapp.com
{:ua "AppleCoreMedia/1.0.0.12F69 (Apple TV; U; CPU OS 8_3 like Mac OS X; en_us)", :browser {:family "Other", :patch nil, :major nil, :minor nil}, :os {:family "ATV OS X", :major "", :minor "", :patch "", :patch_minor ""}, :device {:family "AppleTV", :brand "Apple", :model "AppleTV"}}
```

## Future / Enhancements

I have responded to issues posted to this repository with enhancements I rolled into the codebase, so I do welcome feedback from users. Also, pull requests will be very happily considered.

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
