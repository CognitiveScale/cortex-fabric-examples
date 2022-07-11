# Build Profiles

This example is a CLI application for building Cortex Profiles. This builds off
the [Local Clients](../local-clients/README.md) example for its setup 
(see [ProfileSchemas](../local-clients/README.md#profile-schemas)).

See [BuildProfile](./src/main/java/com/c12e/cortex/examples/profile/BuildProfile.java).

## Jobs

This example builds Profiles for the `member-profile` [Profile Schema](../local-clients/README.md#profile-schemas) by
using pre-built Job flows for:
- Ingesting a DataSource (`IngestDataSourceJob`)
- Building Profiles (`BuildProfileJob`)

These job flows provide existing functionality, similar to what happens when creating these resources in the Cortex Console.

## Running Locally

To run this example locally with local Cortex Clients:
```bash
$ make build

$ ./gradlew main-app:run --args="build-profile -p local -ps member-profile"
16:23:37.979 [main] INFO  org.apache.spark.ui.SparkUI - Bound SparkUI to 0.0.0.0, and started at http://c02wq091htdf.attlocal.net:4040
16:23:38.424 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@4cfa83f9{/metrics/json,null,AVAILABLE,@Spark}
16:23:38.982 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@646cd766{/SQL,null,AVAILABLE,@Spark}
16:23:38.983 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@26457986{/SQL/json,null,AVAILABLE,@Spark}
16:23:38.983 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@b30a50d{/SQL/execution,null,AVAILABLE,@Spark}
16:23:38.984 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@6c742b84{/SQL/execution/json,null,AVAILABLE,@Spark}
16:23:38.993 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@26e0d39c{/static/sql,null,AVAILABLE,@Spark}
16:23:41.702 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: csv, uri: ./src/main/resources/data/members_100_v14.csv, extra
16:23:46.509 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Inferred schema from sample of connection (CSV) - project: 'local', connectionName: 'member-base-file'
16:23:46.536 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Finished reading connection (CSV) - project: 'local', connectionName: 'member-base-file'
16:23:46.539 [main] WARN  c.c.c.p.v.DataSourceWriteValidator - Additional (unexpected) column found in data source - project: 'local', sourceName: 'member-base-ds', column name: 'email'
16:23:46.539 [main] WARN  c.c.c.p.v.DataSourceWriteValidator - Additional (unexpected) column found in data source - project: 'local', sourceName: 'member-base-ds', column name: 'address'
16:23:46.539 [main] WARN  c.c.c.p.v.DataSourceWriteValidator - Additional (unexpected) column found in data source - project: 'local', sourceName: 'member-base-ds', column name: 'pcp_address'
16:23:46.577 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Overwriting delta table: './build/test-data//cortex-profiles/sources/local/member-base-ds-delta'
16:23:54.482 [main] INFO  c.c.c.p.m.d.DefaultCortexDataSourceWriter - Wrote to data source - project: 'local', sourceName: 'member-base-ds'
16:23:55.408 [main] INFO  c.c.c.p.f.DefaultFeatureReportCalculator - Insufficient dataset size for sampling (actual vs MIN_SAMPLE_SIZE): 100 vs 3364. Using entire dataset
16:24:01.870 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: parquet, uri: ./src/main/resources/data/member_flu_risk_100_v14.parquet, extra
16:24:01.870 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Reading connection from file_path './src/main/resources/data/member_flu_risk_100_v14.parquet'
16:24:02.001 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Overwriting delta table: './build/test-data//cortex-profiles/sources/local/member-flu-risk-file-ds-delta'
16:24:03.625 [main] INFO  c.c.c.p.m.d.DefaultCortexDataSourceWriter - Wrote to data source - project: 'local', sourceName: 'member-flu-risk-file-ds'
16:24:03.932 [main] INFO  c.c.c.p.f.DefaultFeatureReportCalculator - Insufficient dataset size for sampling (actual vs MIN_SAMPLE_SIZE): 100 vs 3364. Using entire dataset
16:24:04.899 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: delta, uri: ./build/test-data//cortex-profiles/sources/local/member-base-ds-delta, extra
16:24:04.899 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Reading connection from file_path './build/test-data//cortex-profiles/sources/local/member-base-ds-delta'
Warning: Nashorn engine is planned to be removed from a future JDK release
16:24:05.395 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: delta, uri: ./build/test-data//cortex-profiles/sources/local/member-flu-risk-file-ds-delta, extra
16:24:05.395 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Reading connection from file_path './build/test-data//cortex-profiles/sources/local/member-flu-risk-file-ds-delta'
root
 |-- profile_id: integer (nullable = true)
 |-- state_code: string (nullable = true)
 |-- city: string (nullable = true)
 |-- state: string (nullable = true)
 |-- zip_code: integer (nullable = true)
 |-- gender: string (nullable = true)
 |-- email: string (nullable = true)
 |-- segment: string (nullable = true)
 |-- member_health_plan: string (nullable = true)
 |-- is_PCP_auto_assigned: integer (nullable = true)
 |-- pcp_tax_id: integer (nullable = true)
 |-- address: string (nullable = true)
 |-- phone: string (nullable = true)
 |-- do_not_call: integer (nullable = true)
 |-- channel_pref: string (nullable = true)
 |-- age: integer (nullable = true)
 |-- last_flu_shot_date: string (nullable = true)
 |-- pcp_name: string (nullable = true)
 |-- pcp_address: string (nullable = true)
 |-- _timestamp: timestamp (nullable = false)
 |-- has_phone_number: boolean (nullable = true)
 |-- age_group: string (nullable = true)
 |-- flu_risk_score: double (nullable = true)
 |-- date: string (nullable = true)
 |-- avg_flu_risk: double (nullable = true)
 |-- flu_risk_1_pct: double (nullable = true)
 |-- is_flu_risk_1_pct: boolean (nullable = true)

16:24:06.464 [main] INFO  c.c12e.cortex.phoenix.ProfileEngine - Build Profile Completed
16:24:06.466 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Overwriting delta table: './build/test-data//cortex-profiles/profiles/local/member-profile-delta'
16:24:06.478 [main] WARN  o.a.spark.sql.catalyst.util.package - Truncated the string representation of a plan since it was too large. This behavior can be adjusted by setting 'spark.sql.debug.maxToStringFields'.
16:24:11.422 [main] INFO  c.c.c.p.f.DefaultFeatureReportCalculator - Insufficient dataset size for sampling (actual vs MIN_SAMPLE_SIZE): 100 vs 3364. Using entire dataset
16:24:41.731 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@d946bcc{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
16:24:41.733 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://c02wq091htdf.attlocal.net:4040
```

This will cause the Profiles for the [member-profile](../main-app/src/main/resources/spec/profileSchemas.yml) Profile
Schema to be created by ingesting and joining the `member-base-ds` and `member-flu-risk-file-ds`
[DataSources](../main-app/src/main/resources/spec/datasources.yml). The Profile Schema will additionally have various
computed and bucketed attributes.

The built profiles will be saved at: `main-app/build/test-data/cortex-profiles/profiles/local/member-profile-delta`.

## Running in a Docker Container with Spark-Submit

To run this example with spark-submit/docker container and local Cortex clients:
```bash
# from the parent directory
make clean build create-app-image

docker run -p 4040:4040 --entrypoint="python" -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
  -v $(pwd)/build-profiles/src/main/resources/conf:/app/conf \
  -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
  -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
profiles-example submit_job.py "{ \"payload\" : { \"config\" : \"/app/conf/spark-conf.json\" } }"
```

## Running as a Skill

TODO.
