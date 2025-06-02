# Changelog

## [2.2.1] - 2025-06-02
### Changed
* Bump `JUnit` to 5.13.0.
* `IntHashSet#retainAll(Collection)` and `IntHashSet#retainAll(IntHashSet)` no longer change the capacity of the set.

### Fixed
* Infinite loop in `IntHashSet` when `retainAll` leaves collections with a power of two number of elements.

## [2.2.0] - 2025-05-26
### Changed
* Protect against numeric overflow when recording errors at the end of the large buffer.
* **CI:** Use `gradle/actions/setup-gradle` action for caching Gradle dependencies.
* **CI:** Enable JDK 24 GA build.
* Bump `Gradle` to 8.14.1.
* Bump `Checkstyle` to 10.24.0.
* Bump `ByteBuddy` to 1.17.5.
* Bump `Shadow` to 8.3.6.
* Bump `JUnit` to 5.12.2.
* Bump `Mockito` to 5.18.0.
* Bump `Guava TestLib` to 33.4.8-jre.

### Added
* Add `SystemUtil#isMac` method.
* Add tests for file mapping.

## [2.1.0] - 2025-02-26
### Changed
* Move `get` method declaration to the `ReadablePosition` class. ([eb3b7d284d](https://github.com/aeron-io/agrona/commit/eb3b7d284dcbb4c5f15ae70db0fe3d920d841588))
* Bump `Gradle` to 8.13.
* Bump `Checkstyle` to 10.21.3.
* Bump `ByteBuddy` to 1.17.1.
* Bump `Shadow` to 8.3.6.
* Bump `JUnit` to 5.12.0.

### Added
* Add `compareAndExchange` methods to `AtomicBuffer`. ([#334](https://github.com/aeron-io/agrona/pull/334)) 
* Add `getAndAddPlain` to `AtomicCounter`. ([#328](https://github.com/aeron-io/agrona/pull/328))
* Add `acquire/release` methods to `AtomicBuffer`.  ([#314](https://github.com/aeron-io/agrona/pull/314))
* Add `acquire/release` methods to `AtomicCounter`.  ([#315](https://github.com/aeron-io/agrona/pull/315))
* Add `acquire/release` methods to `Position`.  ([#316](https://github.com/aeron-io/agrona/pull/316))
* Add `plain` methods to `AtomicCounter`.  ([#317](https://github.com/aeron-io/agrona/pull/317))
* Add `opaque` methods to `AtomicCounter`.  ([#319](https://github.com/aeron-io/agrona/pull/319))
* Add `opaque` methods to `AtomicBuffer`.  ([#313](https://github.com/aeron-io/agrona/pull/313))
* Add `opaque` methods to `Position`.  ([#324](https://github.com/aeron-io/agrona/pull/324))
* Add `timestampRelease` method to `MarkFile`.  ([#318](https://github.com/aeron-io/agrona/pull/318))
* Add different flavors of concurrent methods to `StatusIndicator`.  ([#323](https://github.com/aeron-io/agrona/pull/323))

### Fixed
* **CI:** Fix crash logs upload on Windows + compress test data before upload.
* Make `UnsafeApi#arrayBaseOffset` forwards compatible with JDK 25+ which changed the return type to `long` whereas we keep it as `int`.

## [2.0.1] - 2025-01-14
### Changed
* Deprecate `ThreadHints`. ([#312](https://github.com/aeron-io/agrona/pull/312))
* Improve ordering/atomic doc in AtomicBuffer. ([#309](https://github.com/aeron-io/agrona/pull/309))
* Bump `Mockito` to 5.15.2.
* Bump `Checkstyle` to 10.21.1.

### Added
* Add a new convenience constructor to `SleepingIdleStrategy`. ([#310](https://github.com/aeron-io/agrona/pull/310))
* **CI:** Add JDK 25-ea to the build matrix.


## [2.0.0] - 2024-12-17
### Changed
* Fail build on JavaDoc errors.
* Use JUnit BOM.
* **CI:** Disable auto-detection of JVMs to force a specific JVM for test execution in CI.
* Use Gradle's version catalog feature for declaring dependencies.
* Improve RingBuffer tests by reading one message at a time and minimizing the number of valid states.
* Bump `Gradle` to 8.11.1.
* Bump `Checkstyle` to 10.21.0.
* Bump `ByteBuddy` to 1.15.11.
* Bump `bnd` to 7.1.0.
* Bump `Shadow` to 8.3.5.
* Bump `JUnit` to 5.11.4.
* Bump `Guava TestLib` to 33.4.0-jre.

### Added
* Add API to compute `CRC-32C` (`org.agrona.checksum.Crc32c`) and `CRC-32` (`org.agrona.checksum.Crc32`) checksums.

  _**Note:** Requires `--add-opens java.base/java.util.zip=ALL-UNNAMED` JVM option at run time in order to use these classes._

* Add concurrent tests for `getAndAdd` and `getAndSet` operations.

### Removed
* **Breaking:** Remove `org.agrona.UnsafeAccess`. Use `org.agrona.UnsafeApi` instead.

  _**Note:** `--add-opens java.base/jdk.internal.misc=ALL-UNNAMED` JVM option must be specified in order to use `org.agrona.UnsafeApi`._
* **Breaking:** Remove `org.agrona.concurrent.MemoryAccess` was removed. Use either an equivalent APIs provided by
* `org.agrona.UnsafeApi` or `java.lang.invoke.VarHandle`.

* **Breaking:** Remove `org.agrona.concurrent.SigIntBarrier`. Use `org.agrona.concurrent.ShutdownSignalBarrier` instead.

### Fixed
* **Doc:** Remove reference to java 8 ([#304](https://github.com/aeron-io/agrona/pull/304))
* Stop allocating on addAll / removeAll on ObjectHashSet. ([#308](https://github.com/aeron-io/agrona/pull/308))
* Run `Mockito` as Java agent to avoid warning on JDK 21+.

[2.2.1]: https://github.com/aeron-io/agrona/releases/tag/2.2.1
[2.2.0]: https://github.com/aeron-io/agrona/releases/tag/2.2.0
[2.1.0]: https://github.com/aeron-io/agrona/releases/tag/2.1.0
[2.0.1]: https://github.com/aeron-io/agrona/releases/tag/2.0.1
[2.0.0]: https://github.com/aeron-io/agrona/releases/tag/2.0.0