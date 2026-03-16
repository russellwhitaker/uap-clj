# Library Usage

uap-clj can be used as a Clojure library, from a Clojure REPL, or from native Java code.

## Setup

Add to your `deps.edn`:

```clojure
uap-clj/uap-clj {:mvn/version "1.9.0"}
```

Or for Leiningen `project.clj`:

```clojure
[uap-clj "1.9.0"]
```

## Clojure API

### Core functions

```clojure
(require '[uap-clj.core :as uap])

;; Parse all fields at once
(uap/useragent "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
;; => {:ua "Mozilla/5.0 ...",
;;     :browser {:family "Chrome", :major "120", :minor "0", :patch "0"},
;;     :os {:family "Mac OS X", :major "10", :minor "15", :patch "7", :patch_minor ""},
;;     :device {:family "Mac", :brand "Apple", :model "Mac"}}
```

### Individual lookups

If you only need one category, use the individual functions to avoid unnecessary work:

```clojure
(require '[uap-clj.browser :refer [browser]]
         '[uap-clj.os :refer [os]]
         '[uap-clj.device :refer [device]])

(browser "Mozilla/5.0 ... Chrome/120.0.0.0 ...")
;; => {:family "Chrome", :major "120", :minor "0", :patch "0"}

(os "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) ...")
;; => {:family "iOS", :major "17", :minor "0", :patch "", :patch_minor ""}

(device "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) ...")
;; => {:family "iPhone", :brand "Apple", :model "iPhone"}
```

### Memoized variants

For production use where low latency on repeated lookups matters more than memory:

```clojure
(require '[uap-clj.core :refer [useragent-memoized]]
         '[uap-clj.browser :refer [browser-memoized]]
         '[uap-clj.os :refer [os-memoized]]
         '[uap-clj.device :refer [device-memoized]])

;; First call does the parse; subsequent calls with the same input
;; return the cached result instantly
(useragent-memoized "Mozilla/5.0 ...")
```

### Unknown useragents

Unrecognized useragents are classified as `"Other"`:

```clojure
(uap/useragent "some-unknown-agent/1.0")
;; => {:ua "some-unknown-agent/1.0",
;;     :browser {:family "Other", :major nil, :minor nil, :patch nil},
;;     :os {:family "Other", :major nil, :minor nil, :patch nil, :patch_minor nil},
;;     :device {:family "Other", :brand nil, :model nil}}
```

### Spec validation

`clojure.spec` definitions are available for all output maps:

```clojure
(require '[clojure.spec.alpha :as s]
         '[uap-clj.spec])

(s/valid? :uap-clj.spec/useragent (uap/useragent "Mozilla/5.0 ..."))
;; => true

(s/explain :uap-clj.spec/browser {:family "Chrome" :major "120" :minor "0" :patch "0"})
;; => Success!
```

## Interactive REPL

Start a REPL with development dependencies:

```console
$ clj -M:dev
Clojure 1.12.4
user=>
```

Then explore:

```clojure
user=> (require '[uap-clj.core :as u])
nil

user=> (def ua "Lenovo-A288t_TD/S100 Linux/2.6.35 Android/2.3.5 Release/02.29.2012 Browser/AppleWebkit533.1 Mobile Safari/533.1 FlyFlow/1.4")
#'user/ua

user=> (clojure.pprint/pprint (u/useragent ua))
{:ua "Lenovo-A288t_TD/S100 ...",
 :browser {:family "Baidu Explorer", :major "1", :minor "4", :patch ""},
 :os {:family "Android", :major "2", :minor "3", :patch "5", :patch_minor ""},
 :device {:family "Lenovo A288t_TD", :brand "Lenovo", :model "A288t_TD"}}
nil
```

## Java API

uap-clj provides a Java-friendly API for direct use in Java projects.

### Maven setup

Add to your `pom.xml`:

```xml
<repository>
  <id>clojars</id>
  <url>https://repo.clojars.org/</url>
</repository>
```

```xml
<dependency>
  <groupId>org.clojure</groupId>
  <artifactId>clojure</artifactId>
  <version>1.12.4</version>
</dependency>
<dependency>
  <groupId>uap-clj</groupId>
  <artifactId>uap-clj</artifactId>
  <version>1.9.0</version>
</dependency>
```

### Java example

```java
import java.util.HashMap;
import uap_clj.java.api.*;

public class Parser {
  public static void main(String[] args) {
    String ua = args[0];

    HashMap b = Browser.lookup(ua);
    HashMap o = OS.lookup(ua);
    HashMap d = Device.lookup(ua);

    System.out.println("Browser: " + b.get("family") + " " + b.get("major"));
    System.out.println("OS: " + o.get("family") + " " + o.get("major"));
    System.out.println("Device: " + d.get("family") + " (" + d.get("brand") + ")");
  }
}
```

### Return values

All Java API methods return a `HashMap<String, String>`:

| Method            | Keys                                                        |
|-------------------|-------------------------------------------------------------|
| `Browser.lookup`  | `family`, `major`, `minor`, `patch`                         |
| `OS.lookup`       | `family`, `major`, `minor`, `patch`, `patch_minor`          |
| `Device.lookup`   | `family`, `brand`, `model`                                  |
