## Init

$ android create project \
    --target android-23 \
    --name Gobbledygook \
    --path . --activity Gobbledygook \
    -t android-23 \
    -g -v 1.3.1 \
    --package io.tengentoppa.gobbledygook

## Dependencies

The following dependencies are necessary to build/test the project
(the corresponding ArchLinux package names in parentheses):

1) Android SDK (android-sdk)
2) Android Build Tools (android-sdk-build-tools)
3) Android Platform Tools (android-sdk-platform-tools)
4) Android Platform (android-platform)
5) Android Support Repository (android-support-repository)

## Build

$ ./gradlew assembleDebug     # apk Files generated in the builds/ directory
