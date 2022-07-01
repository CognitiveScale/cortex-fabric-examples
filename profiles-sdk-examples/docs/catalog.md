# Cortex Catalog

> The Cortex Catalog is a registry for Agents, Skills, Types, Connections, Data Sources, Profiles, and Campaigns.
(From: https://cognitivescale.github.io/cortex-fabric/docs/reference-guides/cortex-service-descriptions)

The Profiles SDK includes a client for the Cortex Catalog that utilizes
the [Cortex Fabric GraphQL API](https://cognitivescale.github.io/cortex-fabric/graphql-6.2.2/index.html). While deployed
Agents will interact with the actual Cortex Catalog instance, the Profiles SDK includes a local implementation of the
Catalog that may be easier to use during development.

Base Interface: `com.c12e.cortex.phoenix.Catalog` <!-- TODO: Link to javadoc -->

## Remote Catalog

By default, the Profiles SDK (`CortexSession`) will utilize remote Cortex Catalog. This is such that deployed Agents
containing your application (and the Cortex Profiles SDK) will communicate with the Cortex Catalog instance in your
Cortex Cluster.

Implementation: `com.c12e.cortex.profiles.catalog.CortexRemoteCatalog` <!-- TODO: Link to javadoc -->

### Configuration Properties

The `CortexRemoteCatalog` implementation requires:
* The Cortex API endpoint to be specified (`https://api.<dci-base-domain>`) - `spark.cortex.client.phoenix.url`
* A Cortex API Token - `spark.cortex.client.phoenix.token`

**When packaged as a skill with existing templates, the Token and URL will be handled by the Cortex.**
These configuration options need only be set when running outside the Cortex cluster (i.e. locally). This can be useful
to utilize DataSources and Connections defined in the Cortex Cluster locally.

**Disclaimer**: The `CortexRemoteCatalog` implementation is being developed and not all methods (GraphQL endpoints) are
currently supported. Unsupported methods will throw a `NotImplementedError` or `RuntimeException`.

### Managed Content and Profile Data
<!-- TODO(LA): Not sure about this section? Seems like something most "users" won't be able to do. -->

**NOTE**: In order to access Managed Content or Profiles data from a remote Cortex Cluster, you will need to set the
related [configuration properties](./config.md#cortex-backend-storage) to access backing storage for the cluster. **When
packaged in an Agent, the above configuration is handled by Cortex.** These configuration options only need to be set when:
* the application is running the outside the Cortex cluster
* the application needs access to Managed Content or Profile data from a Cortex Cluster

## Local Catalog

The `LocalCatalog` provides a file based implementation for mocking the actual Cortex Catalog. This can be useful while developing
your solution because
- Cortex Resources (DataSources, Connections, and ProfileSchemas) can be written locally in YAML
- Smaller datasets can be used for DataSources and Connections before actual deployment
- Local files can be used for datasets, which need not require Secrets or authentication in general (NOTE: remote connections are supported).
- Authentication to Cortex is not required

**Disclaimer**: The `LocalCatalog` implementation is being developed and not all methods (GraphQL endpoints) are currently supported. Unsupported methods will throw a `NotImplementedError`.
See [Supported Resources](#supported-resources) for which Cortex resources can be used with the `LocalCatalog`.

**NOTE: The `LocalCatalog` implementation is currently project-unaware**, in that:
- Resources are keyed based on their name and are not necessarily scoped to a project
- a default `"local"` project is included
<!-- FeatureSets are only loaded into the "local" project (inconsistency). -->

Implementation: `com.c12e.cortex.phoenix.LocalCatalog` <!-- TODO: Link to javadoc -->

### Configuration options

 To use the `LocalCatalog` instance you will at need to:
- Specify the Catalog implementation (classpath) - `spark.cortex.catalog.local.dir`
- Specify the local directory for the Catalog - `spark.cortex.catalog.impl`

See [./config.md](./config.md#local-development) for more information on configuration options.


### Local File Setup

Below instructions are setting up a local catalog directory.

* Create a directory to contain all Cortex Resources -  `mkdir spec/`
* [Resources](#supported-resources) are defined in YAML files with only a single type of per file
* Create a new file for each resource type you would like to load in the Catalog, e.g. 
    ```
    touch spec/datasources.yaml
    touch spec/connections.yaml
    touch spec/profileschemas.yaml
    ```
* Define the resources in each file. The YAML representation expected by the `LocalCatalog` is not too dissimilar from
  the associated [GraphQL Schema definition](https://cognitivescale.github.io/cortex-fabric/graphql-6.2.2/) but there
  are general differences. The general pattern for representing each resource will be shown below with regard
  to `Connections`, but refer to the below sections for more information
  - Each resource should include top level `apiVersion`, `kind`, `metadata`, and `spec` fields
  - The `apiVersion` can be set to `cognitivescale.io/v1`
  - The `kind` should be the name of the resource type - e.g. Connection, DataSource, ProfileSchema 
  - The `name` of the resource should be defined in the `metadata` object
  - All other attributes should be listed in the `spec` object
  - Example:
  ```yaml
  # in spec/connections.yaml
  apiVersion: cognitivescale.io/v1
  kind: Connection
  metadata:
      # the name of the Connection
      name: "member-base"
  spec:
      # all other attributes of the Connection
      title: Member Base File
      description: local csv file with connections
      connectionType: file
      contentType: csv
      allowRead: true
      allowWrite: false
      # parameters can be defined as per - https://cognitivescale.github.io/cortex-fabric/docs/reference-guides/connection-types
      params:
      - name: uri
        value: ./src/main/resources/data/members_100_v14.csv # link to resource type
      - name: csv/header
        value: true
  ```
  
<!-- TODO(LA):
    - should some/most of this be replaced w/ link to the Javadoc for LocalCatalog?
    - we should really be annotating suggested/supported methods in the source, not listing them
    - not sure extent of resources to include
    - Remote Catalog cannot be created, unless specifying "..."
-->

### Supported Resources

#### Connections

Examples:
```yaml
---
apiVersion: cognitivescale.io/v1
kind: Connection
metadata:
  name: "member-base-file"
spec:
  title: Member Base File
  connectionType: file
  contentType: csv
  allowRead: true
  allowWrite: false
  params:
    - name: uri
      value: ./src/main/resources/data/members_100_v14.csv
    - name: csv/header
      value: true
---
apiVersion: cognitivescale.io/v1
kind: Connection
metadata:
  name: s3-stream
spec:
  title: Member Base S3 File
  connectionType: s3FileStream
  contentType: csv
  allowRead: true
  allowWrite: false
  params:
    - name: uri
      value: s3a://bucket/data-streaming/members_100_v14.csv
    - name: publicKey
      value: ***
    - name: secretKey   # connections must contain credentials
      value: ***
    - name: pathStyleAccess
      value: true
    - name: csv/header
      value: true
    - name: streamReadDir
      value: s3a://bucket/data-streaming
    - name: isTriggered
      value: false
    - name: pollInterval
      value: 10
    - name: csv/multiline
      value: true
    - name: csv/header
      value: true
```

Supported Catalog methods:
- `listConnections`
- `getConnection`
- `saveConnection`
- `deleteConnection`

Reference:
* [Cortex Connection GraphQL Schema](https://cognitivescale.github.io/cortex-fabric/graphql-6.2.2/connection.doc.html)
* [Cortex Connection Types Reference](https://cognitivescale.github.io/cortex-fabric/docs/reference-guides/connection-types)
* [Cortex Connections](https://cognitivescale.github.io/cortex-fabric/docs/manage-data/manage-connections)

#### DataSources
```yaml
---
apiVersion: cognitivescale.io/v1
kind: DataSource
metadata:
  name: member-flu-risk-file-ds
spec:
  kind: batch
  primaryKey: member_id
  connection:
    name: member-flu-risk-file
  attributes:
    - flu_risk_score
    - date
    - member_id
---
apiVersion: cognitivescale.io/v1
kind: DataSource
metadata:
  name: s3-stream-write
spec:
  kind: streaming
  primaryKey: member_id
  connection:
    name: s3-stream
  attributes:
    - member_id
    - state_code
    - city
```

Supported Catalog Methods:
* `listDataSources`
* `getDataSource`
* `saveDataSource`
* `deleteDataSource`
 
Resources: 
* [Cortex DataSource GraphQL Schema](https://cognitivescale.github.io/cortex-fabric/graphql-6.2.2/datasource.doc.html)
* [Cortex DataSource Management](https://cognitivescale.github.io/cortex-fabric/docs/manage-data/manage-data-sources)

#### ProfileSchemas

Examples:
```yaml
---
# bare-bones profile schema from only a single data source
apiVersion: cognitivescale.io/v1
kind: ProfileSchema
metadata:
  name: simple-profile
spec:
  names: 
    title: profile
    singular: profile
    plural: profiles
  primarySource:
    name: datasource-name
    profileKey: profile_key
    profileGroup: default
    timestamp:
        auto: true
    attributes:
      - profile_key
      - attribute1
      - attribute2
      - attribute3
  joins: []
  attributes: []
---
apiVersion: cognitivescale.io/v1
kind: ProfileSchema
metadata:
  # profile schema name defined in 'metadata'
  name: member-profile
spec:
  names:
    title: Member
    singular: member
    plural: members
    categories:
      - healthcare
      - customer
  # Source used in this profile schema
  primarySource:
    name: member-subset
    profileKey: member_id
    profileGroup: Demographics
    timestamp:
      auto: true
    attributes:
      - member_id
      - phone
      - age_group
  # List of DataSources to join into this Profile schema
  joins:
    - name: member-flu-risk-file-ds     # DataSource Name
      join:
        primarySourceColumn: member_id  # column in the primary source to join on
        joinSourceColumn: member_id     # column in the join source to join on
      profileGroup: Inferences
      timestamp:
        field: date
        format: yyyy-MM-dd # Default is MM-dd-yyyy HH:mm:ss.SSS
      attributes:
        - flu_risk_score
  # Specify calculated attributes (custom & bucketed)
  attributes:
    - name: has_phone_number
      profileGroup: Demographics
      source:
        name: member-base-file-ds
      type: custom
      expression: phone.notEqual('').or(phone.notEqual(null))   # ProfileScript expression for custom attribute

    - name: age_group
      profileGroup: Demographics
      source:
        name: member-base-file-ds
      type: bucket
      buckets:
        - name: Under 30
          filter: age.lt(30)
        - name: 30-45
          filter: age.gte(30).and(age.lt(45))
        - name: 45-55
          filter: age.gte(45).and(age.lt(55))
        - name: 55-65
          filter: age.gte(55).and(age.lt(65))
        - name: 65+
          filter: age.gte(65)
```

Implemented Methods
* `listProfileSchemas`
* `getProfileSchema`
* `createProfileSchema`
* `saveProfileSchema`
* `deleteProfileSchema`

Reference:
* [Cortex ProfileSchema GraphQL Schema](https://cognitivescale.github.io/cortex-fabric/graphql-6.2.2/profileschema.doc.html)
* [Cortex Building Profile Schemas](https://cognitivescale.github.io/cortex-fabric/docs/build-profiles/build-schemas)

<!-- Don't think we need these? Will iterate on
#### Projects
- listProjects
- getProject
- saveProject
- deleteProject

#### FeatureSets
- getFeatureSetBySourceName
- saveFeatureSet

#### DataSinks
- listDataSinks
- getDataSink
- saveDataSink
- deleteDataSink

#### Profile Links
- saveProfileLink
- getProfileLink

### Using non-local sources

TODO: Add note that credentials need to be set locally?
-->

### Local Managed Content and Profile Data

<!-- TODO(LA): Not sure about this section? Seems like something most users might not want to do. -->

The Profiles SDK supports configuring a local filesystem as the backend Cortex Storage. To use a local filesystem as the
backend storage set the corresponding [configuration properties](./config.md#cortex-backend-storage).

This is useful when working locally, but may require additional setup. For example, a `DataSource` or `ProfileSchema`
may be defined in the Local Catalog but the corresponding data may not already exist in the `Profiles Bucket`.

## Resources
* [Cortex Fabric GraphQL API Reference](https://cognitivescale.github.io/cortex-fabric/graphql-6.2.2/index.html)