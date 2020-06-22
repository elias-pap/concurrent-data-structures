#!/bin/bash
archiveName=`date +"%Y-%m-%d-%H..%M"`
archivePath="./experiments/raw/$archiveName"
mkdir -p "$archivePath"
cp ./build/*.csv ./build/*.txt $archivePath
