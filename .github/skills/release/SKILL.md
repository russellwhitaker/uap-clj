---
name: release
description: "Release uap-clj to Clojars and GitHub. Use when bumping version, publishing a release, deploying to Clojars, creating a GitHub Release, or cutting a new version. Covers version bump, testing, Clojars deploy, tagging, GitHub Release, native-image CI verification, and upstream sync."
compatibility: "Requires Clojure CLI, gh CLI, CLOJARS_USERNAME and CLOJARS_PASSWORD env vars, and UPSTREAM_PUSH_TOKEN GitHub secret."
metadata:
  author: russellwhitaker
  version: "1.0"
---

# Release Workflow

Complete procedure for releasing a new version of uap-clj.

## Prerequisites

- Clean working tree on a feature branch (never release from `master` directly)
- `gh` CLI authenticated
- `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment variables set
- All tests passing

## Procedure

### 1. Bump the version

Edit `build.clj` and update the `version` string:

```clojure
(def version "X.Y.Z")
```

This single value drives the JAR filename, POM version, git tag, and GitHub Release name.

### 2. Run the full test suite

```sh
clojure -T:build test
```

All 21 tests (113,000+ assertions) must pass. Do not proceed if any fail.

### 3. Check formatting

```sh
clojure -M:cljstyle-check
```

Fix any issues with `clojure -M:cljstyle-fix` before continuing.

### 4. Check for outdated dependencies

```sh
clojure -T:build outdated
```

Address any critical updates before releasing.

### 5. Commit the version bump

```sh
git add build.clj
git commit -m "Bump version to X.Y.Z"
git push origin <branch>
```

### 6. Merge to master

Create a PR, get it merged, then pull master locally:

```sh
git checkout master
git pull origin master
```

### 7. Run the release task

```sh
clojure -T:build release
```

This single command performs:
1. **Pre-flight checks**: fetches remote tags, verifies the tag doesn't already exist
2. **Clojars deploy**: builds the JAR and deploys to Clojars (uses `-Djava.net.preferIPv4Stack=true` from `:build` alias)
3. **Git tag**: creates annotated tag `vX.Y.Z`
4. **Push tag**: pushes the tag to origin
5. **GitHub Release**: creates a release with a changelog link

### 8. Verify CI workflows

After the release is published, three GitHub Actions workflows trigger automatically:

#### Upstream sync (`sync-upstream.yml`)
- Mirrors the tag and GitHub Release to `ua-parser/uap-clj`
- Check: `gh api repos/ua-parser/uap-clj/releases/tags/vX.Y.Z --jq '.html_url'`

#### Native image (`native-image.yml`)
- Builds binaries for Linux (amd64) and macOS (arm64)
- Uploads them to the GitHub Release on both origin and upstream
- Polls up to 5 minutes for the upstream release to appear
- Check: look for `uap-clj-linux-amd64` and `uap-clj-macos-arm64` assets on the release page

#### CI (`clojure.yml`)
- Runs tests, formatting check, and outdated check against the new master
- Should pass (you already verified locally)

### 9. Verify Clojars

Confirm the artifact is available:

```sh
curl -s "https://clojars.org/api/artifacts/uap-clj/uap-clj" | python3 -c "import sys,json; print(json.load(sys.stdin)['latest_release'])"
```

Expected output: `X.Y.Z`

## Troubleshooting

### Clojars deploy fails with "Broken pipe"
The `:build` alias already includes `-Djava.net.preferIPv4Stack=true` in its JVM opts. If you still see this, verify the setting is present in `deps.edn` under `:build :jvm-opts`.

### Tag already exists
The release task checks for existing tags before deploying. If you need to re-release, delete the tag first:
```sh
git tag -d vX.Y.Z
git push origin :refs/tags/vX.Y.Z
```
Then also delete the GitHub Release via the web UI or `gh release delete vX.Y.Z`.

### Native image build fails with reflection errors
Check for reflection warnings:
```sh
clojure -e "(set! *warn-on-reflection* true)" -M -m uap-clj.core 2>&1 | grep "Reflection warning"
```
Add type hints (`^String`, `^java.io.Writer`) to resolve. See PR #64 for precedent.

### Upstream release not found by native-image workflow
The `native-image.yml` workflow polls for the upstream release (created by `sync-upstream.yml`) for up to 5 minutes. If `sync-upstream.yml` is slow or fails, check its run logs and re-run if needed. The native-image workflow will emit a warning and skip upstream upload if the release never appears.
