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
  * `TransformerTest`
* Objects that belong to a parent object will now be automatically assigned if the parent is null when added. e.g. a `Terminal`
  with no `ConductingEquipment` will be automatically associated to any `ConductingEquipment` to which it is added.
* Added a helper function to `Breaker` to identify if it is the head of a `Feeder`.
* Added `isVirtual` and `connectionCategory` to `UsagePoint`

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
