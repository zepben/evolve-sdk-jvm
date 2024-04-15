## Contributing ##

Contributions are always welcome, however at a minimum must meet the following conditions:

1. All new classes should be written in Kotlin. Java code will only be accepted if it's an interop test.
2. All changes must follow the code style. For Intellij this can be imported from [here](TODO)
3. MPL headers must be added to all new code files.
4. All patches must have tests.
5. All tests must pass.

# Data model changes #

If you are planning on updating the CIM profile, to avoid unnecessary work it is advisable to discuss your requirements
with us prior to writing code. We recommend following the process documented [here](TODO) if you want to update the model.

## Development Set up ##

Simply import the maven project into your favourite IDE.

Our main style requirements are:
- 4 spaces instead of tabs
- Continuation line indent of 4 spaces
- Line wrap at 160 characters

## Checklist for model change ##

1. Update `pom.xml` to import the correct version of `evolve-grpc`.
2. Model updated and tested.
3. Add/remove methods added to *Service class if new class was added.
4. Descriptions copied from CIM and added as doc comments to new changes (on class, property etc)
5. `FillFields.kt` updated to populate data for tests. Utilise `includeRuntime` if required.
6. Database:
    1. Table class(es) updated. - `com.zepben.evolve.database.sqlite.tables`
   2. New tables added to appropriate database table collections:
       * `com.zepben.evolve.database.sqlite.customer.CustomerDatabaseTables`
       * `com.zepben.evolve.database.sqlite.diagram.DiagramDatabaseTables`
       * `com.zepben.evolve.database.sqlite.network.NetworkDatabaseTables`
   3. Appropriate CIM writer updated:
       * `com.zepben.evolve.database.sqlite.customer.CustomerCimWriter`
       * `com.zepben.evolve.database.sqlite.diagram.DiagramCimWriter`
       * `com.zepben.evolve.database.sqlite.network.NetworkCimWriter`
   4. Appropriate CIM reader updated:
       * `com.zepben.evolve.database.sqlite.customer.CustomerCimReader`
       * `com.zepben.evolve.database.sqlite.diagram.DiagramCimReader`
       * `com.zepben.evolve.database.sqlite.network.NetworkCimReader`
   5. Appropriate service writer updated if a new class was added:
       * `com.zepben.evolve.database.sqlite.customer.CustomerServiceWriter`
       * `com.zepben.evolve.database.sqlite.diagram.DiagramServiceWriter`
       * `com.zepben.evolve.database.sqlite.network.NetworkServiceWriter`
   6. Appropriate service reader updated if a new class was added:
       * `com.zepben.evolve.database.sqlite.customer.CustomerServiceReader`
       * `com.zepben.evolve.database.sqlite.diagram.DiagramServiceReader`
       * `com.zepben.evolve.database.sqlite.network.NetworkServiceReader`
   7. DB version updated. - `com.zepben.evolve.database.sqlite.tables.TableVersion`
    8. Migration written. - `com.zepben.evolve.database.sqlite.cim.upgrade`
        1. ChangeSet written. - `com.zepben.evolve.database.sqlite.cim.upgrade.changesets.ChangeSet*`
        2. ChangeSet added to UpgradeRunner. - `com.zepben.evolve.database.sqlite.cim.upgrade.UpgradeRunner`
        3. ChangeSetValidator written for ChangeSet. - `com.zepben.evolve.database.sqlite.cim.upgrade.changesets.ChangeSet*Validator`
        4. ChangeSetValidator added to changeSetValidators. - `com.zepben.evolve.database.sqlite.cim.upgrade.ChangeSetTest`
   9. Add schema tests:
       * `com.zepben.evolve.database.sqlite.customer.CustomerDatabaseSchemaTest`
       * `com.zepben.evolve.database.sqlite.diagram.DiagramDatabaseSchemaTest`
       * `com.zepben.evolve.database.sqlite.network.NetworkDatabaseSchemaTest`
7. Reference resolver(s) added (if new associations).
8. Protobuf/gRPC
    1. *CimToProto(s) updated (including java wrapper).
    2. *ProtoToCim(s) updated (including java wrapper).
    3. *TranslatorTest(s) updated.

NOTE: Do not update the StupidlyLargeNetwork file, this will be phased out.

1. *ServiceComparator(s) updated and tested.
2. Exhaustive when functions in *ServiceUtils updated if a new class is added. Update *ServiceUtilsTest to match.
3. Release notes updated.
