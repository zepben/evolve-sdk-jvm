#### Release History

| Version | Released |
| --- | --- |
|[0.7.0](#v070)| `22 September 2021` |
|[0.6.0](#v060)| `06 April 2021` |
|[0.5.0](#v050)| `01 February 2021` |
|[0.4.0](#v040)| `12 January 2021` |
|[0.3.0](#v030)| `10 November 2020` |
| [0.2.0](#v020) | `08 October 2020` |
| [0.1.0](#v010) | `07 September 2020` |

---

NOTE: This library is not yet stable, and breaking changes should be expected until a 1.0.0 release.

---

### v0.7.0

##### Breaking Changes

* `NetworkHierarchy` has been updated to use CIM classes.
* `getFeeder` has been deprecated. Prefer the use of the more generic `getEquipmentContainer`.
* `getIdentifiedObject` now returns a `NoSuchElementException` error if the requested object does not exist rather than a `null` successful result.
* All `CimConsumerClient` implementations have been changed to control a single service rather than one per call.
* `NetworkConsumerClient` has been updated to use the async stub internally and has received a significant performance boost. This only has an impact if you are
  passing the stub directly to the client rather than a channel.
* All SDK methods that retrieve objects with references will now request the network hierarchy first to provide a consistent result, regardless of call order.
* Primitive values on CIM classes are now nullable to allow differentiation between missing and zero values.
* Fixed typo in `NetworkServiceComparator`. Was previously `NetworkServiceCompatatorOptions`.
* Changed `DiagramObject.style` to be a string and removed `DiagramObjectStyle` enum.
* Updated to use v0.15.0 gRPC protocols.
* Moved `ConnectivityResult` from package `com.zepben.evolve.services.network.tracing` to
  `com.zepben.evolve.services.network.tracing.connectivity`.

##### New Features

* Added normal and current version of the connected equipment trace. See `Tracing` for details.
* Added a generic API call, `getEquipmentContainer`, for populating the `Equipment` of an
  `EquipmentContainer`.
* Added API calls for getting loops.
* Added `RunStreaming` examples to the tests, which can be used to talk to a real server.
* Added the following CIM classes:
  * `EquivalentBranch`
  * `EquivalentEquipment`
  * `NoLoadTest`
  * `OpenCircuitTest`
  * `ShortCircuitTest`
  * `ShuntCompensatorInfo`
  * `TransformerTest`
* Objects that belong to a parent object will now be automatically assigned if the parent is null when added. e.g. a `Terminal`
  with no `ConductingEquipment` will be automatically associated to any `ConductingEquipment` to which it is added.
* Added a helper function to `Breaker` to identify if it is the head of a `Feeder`.
* Added `isVirtual` and `connectionCategory` to `UsagePoint`
* Created new traces for tracing connectivity using `ConnectivityResult`. These are available via `Tracing`.

##### Enhancements

* `NetworkHierarchy` now contains circuits and loops.
* Speed of `BaseService.get` has been improved when an explict type is not provided.
* `getIdentifiedObjects` can now take a `Sequence` of mRIDs in addition to an `Iterable` for performance reasons.
* `GrpcChannel` now attempts to clean up channels that fail to shut down.

##### Fixes

* `getIdentifiedObjects` now adds unknown mRIDs to the failed collection.
* Fixed an error in the typing for `GrpcResult` that allowed you to set a value to `null` without specifying a nullable type.
* Fixed errors in the decoding fromm protobuf of optional reference mRIDs that were not assigned.
* Corrected string conversion of `ServiceDifferences` with `NameType` differences.

##### Notes

* None.

---

### v0.6.0

##### Breaking Changes
* `GrpcChannelFactory.create()` now returns a `GrpcChannel`, which is a wrapper around a `Channel` or `ManagedChannel`. This should only be breaking for Java
  users.
* `BatteryUnit` `ratedE` and `storedE` are now `Long` instead of `Double`.
* The package for `DownstreamTree` has been changed from `*.tracing` to `*.tracing.tree`, you will need to reimport.
* `DownstreamTree.TreeNode` has been moved to a top level class in the `*.tracing.tree` package.
* Converted the remaining tracing classes to Kotlin. This will require fixing of many getter calls to include a `get*` from Java and removal of the function
  call in Kotlin etc.

##### New Features
* New class `GrpcChannel` that can be used in try-with-resources blocks when communicating with a Grpc server.
  
* NetworkConsumerClient has 4 new functions:
    - For fetching equipment for an EquipmentContainer

          getEquipmentForContainer(service: NetworkService, mrid: String)         
    - For fetching current equipment for a Feeder

          getCurrentEquipmentForFeeder(service: NetworkService, mrid: String)         
    - For fetching equipment for an OperationalRestriction

          getEquipmentForRestriction(service: NetworkService, mrid: String)         
    - For fetching terminals for a ConnectivityNode

          getTerminalsForConnectivityNode(service: NetworkService, mrid: String)         

* `NetworkConsumerClient.get_feeder()` now resolves all references, and thus you can expect to receive a Feeder with all equipment and their associations populated.

* New class `SwitchStateClient` that allows you to set the current state of switches via a gRPC service.

* BaseService has two new functions which allow retrieving the UnresolvedReferences for an mRID by either `toMrid` or `from.mRID`:
  - getUnresolvedReferencesTo(mRID: String): Sequence<UnresolvedReference<*,*>>
  - getUnresolvedReferencesFrom(mRID: String): Sequence<UnresolvedReference<*,*>>

* Added the following classes:
  * `LoadBreakSwitch`
  * `TransformerEndInfo`
  * `TransformerTankInfo`
  * `TransformerStarImpedance`
  * `Name`
  * `NameType`

* New class `JwtCredentials` added to support managing OAuth2 refresh tokens from Auth0. To be using with the gRPC Consumer and Producer clients which now also
  accept `JwtCredentials` or other `CallCredentials` at initialisation to apply to the stub.

##### Enhancements
* Added some better testing mechanism for database upgrades.
* Instantiating database reader / writer classes is now faster. (Mainly benefits tests)

##### Fixes
* `Resolvers.powerElectronicsConnection(powerElectronicsUnit: PowerElectronicsUnit).` 
  `Resolvers.powerElectronicsConnection(powerElectronicsConnectionPhase: PowerElectronicsConnectionPhase)` 
  `Resolvers.powerElectronicsUnit(powerElectronicsConnection: PowerElectronicsConnection)` 
  `Resolvers.powerElectronicsConnectionPhase(powerElectronicsConnection: PowerElectronicsConnection)` 
  now all have their reverse resolver assigned.
  
##### Notes
* Name and NameType classes enable the ability to give IdentifiedObjects multiple names.

---

### v0.5.0

##### Breaking Changes
 None.

##### New Features

* Added the following classes:
  * `BatteryUnit`
  * `BusbarSection`
  * `PhotoVoltaicUnit`
  * `PowerElectronicsConnection`
  * `PowerElectronicsConnectionPhase`
  * `PowerElectronicsWindUnit`

##### Enhancements
* None.

##### Fixes
* None.

##### Notes
* None.

---

### v0.4.0

##### Breaking Changes
* Updated logic for the PhaseInferrer. When trying to fix missing phases it will first check if it has nominal phase data available and use it before trying to infer the phase.
* The `AssignToFeeders` was changed to assign all `ConductingEquipment` on the `normalHeadTerminal` side
  (inclusive) stopping at open points, head equipment for other feeders and substation transformers.
* You can no longer run `AssignToFeeders` directly on a feeder.
* `getIdentifiedObjects` in the consumer clients now returns a `GrpcResult` of a `MultiObjectResult`, which includes the map of objects added plus a new field
  `failed` which is the set of mRIDs that were not added to the service because another object with the same `mRID` already existed.
* `getFeeder` in `NetworkConsumerClient` now returns a `GrpcResult` as per `getIdentifiedObjects`
* `addFromPb` now return nullable types. They will return null when adding to the service returns `false`.
* `NetworkProtoToCim.toCim(ConnectivityNode)` will no longer update an existing ConnectivityNode if it already exists in the service. This brings the
  serialisation into line with all other `toCim()` functions, however means that deserialising the same `ConnectivityNode` multiple times will now fail rather
  than merging the `ConnectivityNode`s.
* `Equipment` is now added to a `Feeder` even if the phasing is broken. Side effect of this is un-ganged normal switching will no longer assign feeders
  correctly.
* Packages have been reworked, you will need to update your imports.
* Removed obsolete `PositionPointParser`.
* Improved connectivity when using XY phasing.
* Default value for transformerUtilisation property in PowerTransformer class was updated from 0.0 to NaN.
* Added PowerTransformerInfo class.
* Updated PowerTransformer's assetInfo property to hold instance of PowerTransformerInfo class.

##### New Features

* None.

##### Enhancements
* Updated `Feeder` to allow for updating the `normalHeadTerminal` if the feeder has no equipment assigned.
* Added transformer utilisation property to PowerTransformer class
* PhaseInferrer now supports Neutral phase.

##### Fixes

* None.

##### Notes

* None.

---

### v0.3.0

##### Breaking Changes

* `tryGrpc` no longer throws. Helper methods have been added to `GrpcResult` providing convenient ways of maintaining the previous functionality.

##### New Features

* Added `getFeeder` helper to the `NetworkConsumerClient`.
* Added `customer` and `diagram` consumer clients.
* Added `MetadataCollection` allowing you to specify `DataSource` information.

##### Enhancements

* The gRPC consumer and producer clients now share a common base class.
* Added primaryVoltage property to PowerTransformer for convenience.

##### Fixes

* Fixed null annotations on `ConnectivityResult`.

##### Notes

* None.

---

### v0.2.0

##### Breaking Changes

* `ConnectivityResult` is now immutable.

##### New Features

* None.

##### Enhancements

* Tweaked error handling in the `put` package.

##### Fixes

* Database upgrade now takes its backup before changing the version table to change sets.
* `RemovePhases` now supports SWER.
* `FindWithUsagePoints` now supports first terminal sequence numbers greater than one.

##### Notes

* None.

---

### v0.1.0

Initial release containing the Zepben CIM profile, database [de]serialisation, protobuf [de]serialisation, network model tracing, and gRPC producer clients.

