cmake_minimum_required(VERSION 3.25)

project(WhatHappenedToMyMic LANGUAGES CXX)

find_package(JNI REQUIRED)

add_library(WHTMMENGINE SHARED
        whtmm/engine.cpp
        whtmm/JNI.cpp
)

target_compile_features(WHTMMENGINE PRIVATE cxx_std_20)

target_include_directories(WHTMMENGINE PRIVATE
        WHTMMENGINE
        ${JNI_INCLUDE_DIRS}
)

target_link_libraries(WHTMMENGINE PRIVATE
        ole32
        uuid
        propsys
)