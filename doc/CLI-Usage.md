# CLI Usage

uap-clj reads a text file containing one useragent string per line and writes a TSV (tab-separated values) file with parsed browser, OS, and device fields.

## Running the CLI

There are three ways to run uap-clj from the command line:

### Native binary (fastest startup)

```bash
./uap-clj <input_file> [<output_file>]
```

Pre-built binaries are available from [GitHub Releases](https://github.com/russellwhitaker/uap-clj/releases). See [Getting Started](Getting-Started.md) for download instructions.

### Uberjar (requires JVM)

```bash
java -jar uap-clj-1.9.0-standalone.jar <input_file> [<output_file>]
```

### Clojure CLI (from source checkout)

```bash
clojure -M -m uap-clj.core <input_file> [<output_file>]
```

## Arguments

| Argument       | Required | Description                                                        |
|----------------|----------|--------------------------------------------------------------------|
| `input_file`   | Yes      | Path to a text file with one useragent string per line             |
| `output_file`  | No       | Path for the TSV output file. Defaults to `useragent_lookup.tsv`   |

## Output format

The output is a tab-separated file with a header row and one data row per input useragent. The columns are:

| Column           | Description                          |
|------------------|--------------------------------------|
| `ua`             | Original useragent string            |
| `browser family` | Browser name (e.g. "Chrome", "Firefox") |
| `browser major`  | Browser major version                |
| `browser minor`  | Browser minor version                |
| `browser patch`  | Browser patch version                |
| `os family`      | Operating system name                |
| `os major`       | OS major version                     |
| `os minor`       | OS minor version                     |
| `os patch`       | OS patch version                     |
| `os patch_minor` | OS patch minor version               |
| `device family`  | Device name                          |
| `device brand`   | Device manufacturer                  |
| `device model`   | Device model identifier              |

Unknown or unrecognized values appear as `"Other"` (for family fields) or empty (for version fields).

## Example

```bash
# Create a sample input file
cat > /tmp/useragents.txt << 'EOF'
Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36
Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1
Lenovo-A288t_TD/S100 Linux/2.6.35 Android/2.3.5 Release/02.29.2012 Browser/AppleWebkit533.1 Mobile Safari/533.1 FlyFlow/1.4
EOF

# Run
./uap-clj /tmp/useragents.txt /tmp/parsed.tsv

# View results
column -t -s $'\t' /tmp/parsed.tsv
```

The TSV output can be directly imported into spreadsheets, databases, or ETL pipelines.

## Building the native binary locally

This requires [GraalVM Community Edition 21+](https://www.graalvm.org/) with the `native-image` tool on your PATH.

```console
$ clojure -T:build native-image
Built target/uap-clj

$ ./target/uap-clj /tmp/useragents.txt /tmp/parsed.tsv
```

If you use [SDKMAN!](https://sdkman.io/):

```bash
sdk install java 21.0.2-graalce
sdk use java 21.0.2-graalce
```
