#!/bin/sh

sbt one-jar

cp rebaser $HOME/bin/rebaser
chmod u+x $HOME/bin/rebaser

JAR_FILE_NAME=`find . -name \*one-jar.jar`
cp $JAR_FILE_NAME $HOME/bin/rebaser.jar

