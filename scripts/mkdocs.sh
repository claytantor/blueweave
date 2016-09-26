#!/usr/bin/env bash

rm docs/javadocs
./gradlew clean javadocs
cp -rf ./build/docs/javadoc ./docs/.