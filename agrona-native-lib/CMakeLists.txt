cmake_minimum_required(VERSION 3.16)
project(agrona-native-lib C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

find_package(JNI REQUIRED)

add_library(agrona-native-lib SHARED
    src/main/c/org_agrona_affinity_ThreadAffinity.c
)

target_include_directories(agrona-native-lib PRIVATE
    src/main/headers
    ${JNI_INCLUDE_DIRS}
    ${JAVA_HOME}/include
    ${JAVA_HOME}/include/linux
)

set_target_properties(agrona-native-lib PROPERTIES
    PREFIX "lib"
    OUTPUT_NAME "agrona-native-lib"
)
