# uap-clj

## Setup
This project depends on the file `regexes.yaml` actively maintained in the [`ua-parser/uap-core`](https://github.com/ua-parser/uap-core) project. This dependency requires using [`lein-git-deps`](https://github.com/torsten/lein-git-deps) to manage a local copy of that core YAML resource.
```bash
% lein deps
% lein git-lein-deps
```
The same dependency management approach is used for ingesting test fixture resources found in `uap-core`.

__Maintained by Russell Whitaker__
