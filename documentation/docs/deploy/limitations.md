---
sidebar_position: 8
---

# Limitations

## Docker setup limitations

What docker setup doesn't support:

* To run workers
* To run satellites
* Overlay works only mounted folders, so it is: conf, centralConfiguration, hotfix, plugins.
* Debugging
* Log levels

Docker image contains all plugins which are defined in Deploy Server Trial distribution.
If you want to exclude some of them you can use property `defaultOfficialPluginsToExclude`.
For example if you want to exclude terraform and aws plugin, you have to configure it as: 

```groovy
defaultOfficialPluginsToExclude = ["terraform", "aws"]
```

## Data Import limitation (available only for the internal use in Digital.ai)

* `postgres` is the only database which fully support data import
* `derby-inmemory`, `derby-network` do not support DbUnit data import, 
as these databases are not supported, use old data export format

## Database Images limitations
* Only  `mysql`, `mysql-8`, `postgres` can be started at the moment with the integration server
* `mssql`, `oracle-19c-se` require building an image at the moment and cannot be started by the integration server
