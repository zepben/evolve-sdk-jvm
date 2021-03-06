[![codecov](https://codecov.io/gh/zepben/evolve-sdk-jvm/branch/main/graph/badge.svg?token=WJKHSV0GS4)](https://codecov.io/gh/zepben/evolve-sdk-jvm)

# Evolve SDK #

The Evolve SDK contains everything necessary to communicate with a [Zepben EWB Server](https://github.com/zepben/energy-workbench-server). See the
complete [Evolve JVM SDK Documentation](https://zepben.github.io/evolve/docs/jvm-sdk/) for more details.

## Requirements ##

- Java 11

## Building ##

Requirements to build

- JDK11 (tested with Amazon Corretto)
- Maven

To build:

    mvn package

## Checklist for model change ##

1. Update `pom.xml` to import the correct version of `evolve-grpc`.
1. Model updated and tested.
1. Descriptions copied from CIM and added as doc comments to new changes (on class, property etc)
1. `FillFields.kt` updated to populate data for tests. Utilise `includeRuntime` if required.
1. Database:
   1. Table class(es) updated. - `com.zepben.evolve.database.sqlite.tables`
   1. New tables added to DatabaseTables - `DatabseTables.kt`
   1. Writer updated. - `com.zepben.evolve.database.sqlite.writers`
   1. Reader updated. - `com.zepben.evolve.database.sqlite.readers`
   1. DB version updated. - `TableVersion.kt`
   1. Migration written. - `com.zepben.evolve.database.sqlite.upgrade`
   1. Add schema tests - `DatabaseSqliteTest.kt`
1. Reference resolver(s) added (if new associations).
1. Protobuf/gRPC
   1. *CimToProto(s) updated (including java wrapper).
   1. *ProtoToCim(s) updated (including java wrapper).
   1. *TranslatorTest(s) updated.

NOTE: Do not update the StupidlyLargeNetwork file, this will be phased out.

1. *ServiceComparator(s) updated and tested.
1. Exhaustive when functions in *ServiceUtils updated if a new class is added.
1. Release notes updated.
