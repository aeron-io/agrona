# Changelog

## [2.3.0-rc1] - 2025-09-03
* **Breaking:** Deprecated `org.agrona.concurrent.SigInt` for removal. Use
`org.agrona.concurrent.ShutdownSignalBarrier` instead.

  _**NB:** `org.agrona.concurrent.SigInt.register(java.lang.Runnable)` is unsafe as it overrides `SIGINT` signal
  handling of the JVM thus preventing shutdown hooks from being executed._

* **Breaking:** Changed `org.agrona.concurrent.ShutdownSignalBarrier` to use shutdown hooks instead of signals.

  Previously `ShutdownSignalBarrier` relied on intercepting `SIGINT` and `SIGTERM`
  [OS signals](https://man7.org/linux/man-pages/man7/signal.7.html) by overriding JVM's signal handling which was
  preventing shutdown hooks from be executed. This in turn was breaking applications and frameworks that relied on
  shutdown hooks for clean termination.

  New implementation uses shutdown hooks instead and requires `ShutdownSignalBarrier` to be explicitly closed
  (typically last).

  _**NB:** Failure to close `ShutdownSignalBarrier` might result in JVM not terminating._
 
  As the result the code using `ShutdownSignalBarrier` needs to be updated:

  - Old (before `2.3.0`):
    ```java
    class UsageSample
    {
        public static void main(final String[] args)
        {
            final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();
            try (MyService service = new MyService())
            {
                barrier.await();
            }
        }
    }
    ```

  - New:
    ```java
    class UsageSample
    {
        public static void main(final String[] args)
        {
            try (ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();
                MyService service = new MyService())
            {
                barrier.await();
            }
        }
    }
    ```
    In the above example `ShutdownSignalBarrier` is closed last to ensure that `service` terminates completely before
    `ShutdownSignalBarrier` closes which in turn allows JVM to exit.

* AtomicCounter minor javadoc improvements. ([#338](https://github.com/aeron-io/agrona/pull/338]))
* Bump `Gradle` to 9.0.0.
* Bump `ByteBuddy` to 1.17.7.
* Bump `Checkstyle` to 11.0.1.
* Bump `JUnit` to 5.13.4.
* Bump `Mockito` to 5.19.0.
* Bump `Shadow` to 9.1.0.

## [2.2.4] - 2025-06-27
### Changed
* Bump `JUnit` to 5.13.2.
* Bump `Checkstyle` to 10.26.0.
* Bump `Shadow` to 8.3.7.

### Fixed
* Fix possible IndexOutOfBoundsException in MarkFile constructor which creates the parent directory. ([#337](https://github.com/aeron-io/agrona/pull/337]))

## [2.2.3] - 2025-06-20
### Changed
* Bump `ByteBuddy` to 1.17.6.
* Bump `JUnit` to 5.13.1.
* Bump `Gradle` to 8.14.2.

### Added
* Add `SystemEpochNanoClock#INSTANCE` constant.

### Removed
* Remove unused and broken `CompilerUtil#compileOnDisk` and `CompilerUtil#persist` methods.

## [2.2.2] - 2025-06-05
### Changed
* Publish release artifacts to Central Portal using OSSRH Staging API service.
* Bump `Checkstyle` to 10.25.0.

## [2.2.1] - 2025-06-02
### Changed
* `IntHashSet#retainAll(Collection)` and `IntHashSet#retainAll(IntHashSet)` no longer change the capacity of the set.
* Bump `JUnit` to 5.13.0.

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

[2.3.0-rc1]: https://github.com/aeron-io/agrona/releases/tag/2.3.0-rc1
[2.2.4]: https://github.com/aeron-io/agrona/releases/tag/2.2.4
[2.2.3]: https://github.com/aeron-io/agrona/releases/tag/2.2.3
[2.2.2]: https://github.com/aeron-io/agrona/releases/tag/2.2.2
[2.2.1]: https://github.com/aeron-io/agrona/releases/tag/2.2.1
[2.2.0]: https://github.com/aeron-io/agrona/releases/tag/2.2.0
[2.1.0]: https://github.com/aeron-io/agrona/releases/tag/2.1.0
[2.0.1]: https://github.com/aeron-io/agrona/releases/tag/2.0.1
[2.0.0]: https://github.com/aeron-io/agrona/releases/tag/2.0.0