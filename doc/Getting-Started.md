# Getting Started

## Prerequisites

- **Java 21** (or later). [Eclipse Temurin](https://adoptium.net/) is recommended:

  ```console
  $ java -version
  openjdk version "21" 2023-09-19 LTS
  OpenJDK Runtime Environment Temurin-21+35 (build 21+35-LTS)
  ```

- **Clojure CLI** (`clojure` / `clj`). Install via [the official guide](https://clojure.org/guides/install).

> **Note:** If you encounter `transfer failed` or `Broken pipe` errors during dependency resolution, your JVM may be failing TLS handshakes over IPv6. Fix this by setting:
> ```bash
> export JAVA_TOOL_OPTIONS="-Djava.net.preferIPv4Stack=true"
> ```

## Option A: Add as a dependency (library use)

Add to your `deps.edn`:

```clojure
uap-clj/uap-clj {:mvn/version "1.9.0"}
```

Then require it:

```clojure
(require '[uap-clj.core :as uap])
(uap/useragent "Mozilla/5.0 ...")
```

See [Library Usage](Library-Usage.md) for full details and examples.

## Option B: Download a pre-built native binary (CLI use)

Pre-built binaries (no JVM required) are attached to each [GitHub Release](https://github.com/russellwhitaker/uap-clj/releases):

| Platform        | Asset name             |
|-----------------|------------------------|
| Linux x86_64    | `uap-clj-linux-amd64`  |
| macOS ARM64     | `uap-clj-macos-arm64`  |

Download and make executable:

```bash
# Linux
curl -L -o uap-clj \
  https://github.com/russellwhitaker/uap-clj/releases/download/v1.9.0/uap-clj-linux-amd64
chmod +x uap-clj

# macOS (Apple Silicon)
curl -L -o uap-clj \
  https://github.com/russellwhitaker/uap-clj/releases/download/v1.9.0/uap-clj-macos-arm64
chmod +x uap-clj
```

Or use the GitHub CLI:

```bash
gh release download v1.9.0 --repo russellwhitaker/uap-clj --pattern 'uap-clj-linux-amd64'
chmod +x uap-clj-linux-amd64
```

See [CLI Usage](CLI-Usage.md) for how to use the binary.

## Option C: Build from source

```bash
git clone https://github.com/russellwhitaker/uap-clj.git
cd uap-clj
git submodule update --init --recursive
clojure -P && clojure -P -M:test
```

Build the standalone jar:

```console
$ clojure -T:build uber
Built target/uap-clj-1.9.0-standalone.jar
```

Or build a native binary (requires [GraalVM CE 21+](https://www.graalvm.org/) with `native-image` on PATH):

```console
$ clojure -T:build native-image
Built target/uap-clj
```

See [Development](Development.md) for the full build guide.

## Quick test

Create a file with one useragent per line:

```bash
echo "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36" > /tmp/ua.txt
```

Run the tool:

```bash
# With the native binary
./uap-clj /tmp/ua.txt /tmp/result.tsv

# With the uberjar
java -jar target/uap-clj-1.9.0-standalone.jar /tmp/ua.txt /tmp/result.tsv

# With the Clojure CLI (from the repo)
clojure -M -m uap-clj.core /tmp/ua.txt /tmp/result.tsv
```

Inspect the output:

```console
$ cat /tmp/result.tsv
ua	browser family	browser major	browser minor	browser patch	os family	os major	os minor	os patch	os patch_minor	device family	device brand	device model
Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36	Other				Mac OS X	10	15	7		Mac	Apple	Mac
```
