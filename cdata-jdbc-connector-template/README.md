# jdbc-connector-skill-template

Cortex Skill Template for Generic and CDATA JDBC connections

Requirements:
- Java >= 8
- Gradle 3.4.x+

uses hikari but doesn't parse the extra params CDATA has../CDATA specific plugin, require extra parsing for CData params.

Contents:
* jdbc-plugin-cdata - generic/cdata JDBC plugin
* jdbc-service-harness - daemon code, load plugins, ...
* jdbc-service-lib - base library for creating plug-ins

Building:
```shell script
make 
```

Running in IDE
Launch using main class `com.networknt.server.Server`

Running from command-line
```
java -jar jdbc-service-harness/build/libs/jdbc-service-harness.jar -Dplugin.jar=./jdbc-plugin-basic/build/libs/jdbc-plugin-basic.jar -agentlib\:jdwp\=transport\=dt_socket,server=y,suspend=n,add
```

Invoking via CURL
```
curl -i -H "Authorization: bearer $TOKEN" -H "Content-Type: application/json" -X POST localhost:8080/invoke -d @./jdbc-service-harness/src/test/resources/testRequest.json
```
TODOs
rebrand to light-4j ?? 