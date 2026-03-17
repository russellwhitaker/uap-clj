# CLAUDE.md — Agent Guidelines for uap-clj

## Git Workflow

- **Never commit or push directly to `master`.** Always work on a feature branch.
- Before any `git push`, verify the current branch with `git branch --show-current` and abort if on `master`.
- Do not force-push or use `--no-verify` unless explicitly asked.

## Testing

- Run `clojure -T:build test` frequently — after any code change, before committing, and after merging.
- All tests must pass before pushing.

## Build & Project

- Build system: deps.edn + tools.build (`build.clj`)
- Key aliases: `:dev`, `:test`, `:build`, `:bench`
- Build tasks: `clojure -T:build test`, `clojure -T:build uber`, `clojure -T:build native-image`
- Formatting: cljstyle (runs via pre-commit hook in `hooks/pre-commit`; install with `cp hooks/pre-commit .git/hooks/pre-commit && chmod +x .git/hooks/pre-commit`)
- Native image requires GraalVM with `native-image` on PATH

## Deploy

- Clojars deploy requires `-Djava.net.preferIPv4Stack=true` (already in `:build` alias JVM opts)
- Use `clojure -T:build deploy` to publish to Clojars

## Skills

- Release workflow: `.github/skills/release/SKILL.md`
