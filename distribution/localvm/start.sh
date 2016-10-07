#! /bin/bash

#
# Note: since this configuration requires virtual box it can't be run inside a docker or VM environment
#

#! /bin/bash

if [[ $1 == "--debug" ]] ; then
    java -jar \
         -Dspring.profiles.active=localvmDocker \
         -Djava.security.egd=file:/dev/./urandom \
         -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 \
         ../../server/target/server.jar
else
    java -jar \
         -Dspring.profiles.active=localvmDocker \
         -Djava.security.egd=file:/dev/./urandom \
         ../../server/target/server.jar
fi


