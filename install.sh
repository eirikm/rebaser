#!/bin/sh

sbt clean one-jar

cp -v rebaser $HOME/bin/rebaser
chmod -v u+x $HOME/bin/rebaser

JAR_FILE_NAME=`find . -name \*one-jar.jar`
cp -v $JAR_FILE_NAME $HOME/bin/rebaser.jar

