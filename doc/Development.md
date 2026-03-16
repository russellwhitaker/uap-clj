# Development

Guide for building, testing, and contributing to uap-clj.

## Clone and setup

```bash
git clone https://github.com/russellwhitaker/uap-clj.git
cd uap-clj
git submodule update --init --recursive
clojure -P && clojure -P -M:test
```

The submodule at `src/resources/submodules` pulls in the [ua-parser/uap-core](https://github.com/ua-parser/uap-core) regex definitions and test fixtures.

## Build system

uap-clj uses [deps.edn](https://clojure.org/reference/deps_edn) with [tools.build](https://clojure.org/guides/tools_build). All build tasks are defined in `build.clj`.

### Key aliases

| Alias             | Purpose                                      |
|-------------------|----------------------------------------------|
| `:dev`            | Development REPL with extra dependencies     |
| `:test`           | Run the test suite                           |
| `:build`          | Build tasks (jar, uber, deploy, etc.)        |
| `:bench`          | Run benchmark suite                          |
| `:cljstyle-check` | Check code formatting                        |
| `:cljstyle-fix`   | Auto-fix code formatting                     |

### Build tasks

```bash
# Run tests
clojure -T:build test

# Build library jar
clojure -T:build jar

# Build standalone uberjar
clojure -T:build uber

# Build GraalVM native binary
clojure -T:build native-image

# Update regexes.edn from upstream YAML
clojure -T:build update-regexes

# Check for outdated dependencies
clojure -T:build outdated

# Deploy to Clojars
CLOJARS_USERNAME=<user> CLOJARS_PASSWORD=<token> clojure -T:build deploy
```

## Testing

The test suite uses [clojure.test](https://clojure.org/guides/test) with the [Cognitect test-runner](https://github.com/cognitect-labs/test-runner). Tests are generated from the ua-parser/uap-core YAML fixtures, covering thousands of real-world useragent strings.

```console
$ clojure -T:build test

Testing uap-clj.browser-test
Testing uap-clj.core-test
Testing uap-clj.device-test
Testing uap-clj.java.api.browser-test
Testing uap-clj.java.api.device-test
Testing uap-clj.java.api.os-test
Testing uap-clj.os-test
Testing uap-clj.spec-test

Ran 21 tests containing 113873 assertions.
0 failures, 0 errors.
```

Run tests frequently — after any code change and before committing.

## Code formatting

The project uses [cljstyle](https://github.com/greglook/cljstyle) for consistent formatting.

Check formatting:

```bash
clojure -M:cljstyle-check
```

Auto-fix formatting:

```bash
clojure -M:cljstyle-fix
```

### Pre-commit hook

A git pre-commit hook is provided to auto-format staged Clojure files:

```bash
cp hooks/pre-commit .git/hooks/pre-commit && chmod +x .git/hooks/pre-commit
```

## Benchmarks

Run the [criterium](https://github.com/hugoduncan/criterium)-based benchmark suite:

```console
$ clojure -M:bench
```

This benchmarks `browser`, `os`, `device`, and `useragent` lookups individually and in batch.

## Updating uap-core

The regex definitions come from the [ua-parser/uap-core](https://github.com/ua-parser/uap-core) submodule. To pull in upstream changes:

```bash
cd src/resources/submodules
git pull origin master
cd ../../..
clojure -T:build update-regexes
clojure -T:build test
```

A [GitHub Actions workflow](https://github.com/russellwhitaker/uap-clj/actions/workflows/update-uap-core.yml) runs weekly to check for uap-core updates and opens a PR automatically when changes are found.

## Building a native binary

Requires [GraalVM Community Edition 21+](https://www.graalvm.org/) with `native-image` on your PATH.

If you use [SDKMAN!](https://sdkman.io/):

```bash
sdk install java 21.0.2-graalce
sdk use java 21.0.2-graalce
```

Then build:

```console
$ clojure -T:build native-image
Built target/uap-clj
```

The binary is self-contained — no JVM needed to run it.

## CI/CD

GitHub Actions workflows:

| Workflow               | Trigger                         | Purpose                                              |
|------------------------|---------------------------------|------------------------------------------------------|
| `clojure.yml`          | Push, PR                        | Tests, formatting check, outdated dependency check   |
| `native-image.yml`     | Release published               | Build native binaries, attach to GitHub Release       |
| `update-uap-core.yml`  | Weekly schedule                 | Check for uap-core updates, open PR if found         |
| `sync-upstream.yml`    | Push to master, release         | Mirror to ua-parser/uap-clj upstream fork            |

## Git workflow

- Never commit or push directly to `master`. Work on a feature branch.
- All tests must pass before pushing.
- PRs are merged to `master` after review.
