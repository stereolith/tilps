# tilps

Tilps helps splitting costs between friends.


## Development setup:
### Database
* run Datomic database (from datomic root):
  `bin/transactor config/dev-transactor.properties`

### Server
Two options to run the API server:

* run server with `lein ring server`
* run server from REPL (i.e. for cider jack-in):
  1. in emacs, `M-x cider-jack-in RET`
  2. in REPL, run `(ring-start)` (function defined in `dev/clj/user.clj` and added to the source path in the `:dev` profile via `project.clj`)
