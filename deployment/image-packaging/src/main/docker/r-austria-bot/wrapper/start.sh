#!/bin/sh
DEBUG_PARAMS=""
if [ "x$REMOTE_DEBUG" = 'xtrue' ]; then
  DEBUG_PARAMS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
  echo "Starting with remote debugging on port 5005"
fi;
java ${DEBUG_PARAMS} -jar app.jar