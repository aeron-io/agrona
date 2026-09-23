cmake_minimum_required(VERSION 3.16)
project(agrona-native-lib C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Only JNI type definitions are needed, so the host JDK's headers suffice. When cross-compiling from macOS the JDK
# ships include/darwin rather than include/linux; its jni_md.h is equivalent on 64-bit targets.
if(EXISTS "${JAVA_HOME}/include/linux")
    set(JNI_PLATFORM_INCLUDE_DIR "${JAVA_HOME}/include/linux")
else()
    set(JNI_PLATFORM_INCLUDE_DIR "${JAVA_HOME}/include/darwin")
endif()

add_library(agrona-native-lib SHARED
    src/main/c/org_agrona_affinity_ThreadAffinity.c
)

target_include_directories(agrona-native-lib PRIVATE
    src/main/headers
    ${JAVA_HOME}/include
    ${JNI_PLATFORM_INCLUDE_DIR}
)

set_target_properties(agrona-native-lib PROPERTIES
    PREFIX "lib"
    OUTPUT_NAME "agrona-native-lib"
)
