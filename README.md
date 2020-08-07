# tilps

Tilps helps splitting costs between friends.


### Development setup:
* run Datomic database (from datomic root):
  `bin/run -m datomic.peer-server -h localhost -p 8998 -a myaccesskey,mysecret -d tilps,datomic:mem://tilps..`
* run server with `lein ring server`

