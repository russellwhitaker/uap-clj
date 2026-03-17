---
name: release
description: "Release uap-clj to Clojars and GitHub. Use when bumping version, publishing a release, deploying to Clojars, creating a GitHub Release, or cutting a new version. Covers version bump, testing, Clojars deploy, tagging, GitHub Release, native-image CI verification, and upstream sync."
compatibility: "Requires Clojure CLI, gh CLI, jq, CLOJARS_USERNAME and CLOJARS_PASSWORD env vars. UPSTREAM_PUSH_TOKEN GitHub secret is needed only for post-release upstream mirroring (sync-upstream/native-image workflows)."
metadata:
  author: russellwhitaker
  version: "1.0"
---

# Release Workflow

Complete procedure for releasing a new version of uap-clj.

## Prerequisites

- Version bump done on a feature branch via PR (never commit directly to `master`)
- Clean working tree on `master` after the version-bump PR is merged
- `gh` CLI authenticated
- `jq` installed (used for Clojars verification)
- `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment variables set
- uap-core submodule initialized (`git submodule update --init --recursive`)
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
clojure -M:test
```

All tests must pass. Do not proceed if any fail.

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
5. **GitHub Release**: creates a release (includes a "Full Changelog" compare link when a previous tag exists; otherwise uses a plain "Release vX.Y.Z" body)

### 8. Verify CI and post-release workflows

#### CI (`clojure.yml`) — already ran on merge to `master`
- Triggered when the version-bump PR was merged (push to `master`)
- Runs tests, formatting check, and outdated check
- Should have passed (you already verified locally in steps 2-4)

After the release task creates the GitHub Release, two additional workflows trigger:

#### Upstream sync (`sync-upstream.yml`) — triggers on `release: published`
- Mirrors the tag and GitHub Release to `ua-parser/uap-clj`
- Check: `gh api repos/ua-parser/uap-clj/releases/tags/vX.Y.Z --jq '.html_url'`

#### Native image (`native-image.yml`) — triggers on `release: published`
- Builds binaries for Linux (amd64) and macOS (arm64)
- Uploads them to the GitHub Release on both origin and upstream
- Polls up to 5 minutes for the upstream release to appear
- Check: look for `uap-clj-linux-amd64` and `uap-clj-macos-arm64` assets on the release page

### 9. Verify Clojars

Confirm the artifact is available:

```sh
curl -s "https://clojars.org/api/artifacts/uap-clj/uap-clj" | jq -r '.latest_release'
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
clojure -M -e "(set! *warn-on-reflection* true) (require 'uap-clj.core)" 2>&1 | grep "Reflection warning"
```
This loads the namespace without invoking `-main` (which requires CLI arguments). Add type hints (`^String`, `^java.io.Writer`) to resolve any warnings. See the `with-open` native-image reflection comment in `uap-clj.core/-main` for precedent.

### Upstream release not found by native-image workflow
The `native-image.yml` workflow polls for the upstream release (created by `sync-upstream.yml`) for up to 5 minutes. If `sync-upstream.yml` is slow or fails, check its run logs and re-run if needed. The native-image workflow will emit a warning and skip upstream upload if the release never appears.
