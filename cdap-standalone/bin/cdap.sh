#!/bin/sh
#
# Copyright © 2014 Cask Data, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.
#

# Add default JVM options here. You can also use JAVA_OPTS and CDAP_OPTS to pass JVM options to this script.
CDAP_OPTS="-XX:+UseConcMarkSweepGC -Djava.security.krb5.realm= -Djava.security.krb5.kdc= -Djava.awt.headless=true"

# Specifies Web App Path
WEB_APP_PATH=${WEB_APP_PATH:-"web-app/local/server/main.js"}

APP_HOME="`pwd -P`"
NUX_FILE="$APP_HOME/.nux_dashboard"

CLASSPATH=$APP_HOME/lib/*:$APP_HOME/conf/
CDAP_HOME=${CDAP_HOME:-/opt/cdap}; export CDAP_HOME
COMPONENT_HOME=${CDAP_HOME}; export COMPONENT_HOME

# PID Location
PID_DIR=/var/tmp

MIN_NODE_VER="v0.8.16"
MIN_NODE_MAJ_VER=8
MIN_NODE_MIN_VER=16


# We need a larger PermSize for SparkProgramRunner to call SparkSubmit
function set_perm_size
{
    if [ -d /opt/cdap ]; then
        CDAP_HOME=/opt/cdap; export CDAP_HOME
        DEFAULT_JVM_OPTS="-Xmx3072m -XX:MaxPermSize=128m"
    else
        DEFAULT_JVM_OPTS="-Xmx1024m -XX:MaxPermSize=128m"
    fi

    # return
    echo $DEFAULT_JVM_OPTS
}

function set_app_home
{
    # $0 - program

    # Resolve symlinks:
    PRG="$0"

    # Need this for relative symlinks.
    while [ -h "$PRG" ] ; do
        ls=`ls -ld "$PRG"`
        link=`expr "$ls" : '.*-> \(.*\)$'`
        if expr "$link" : '/.*' > /dev/null; then
            PRG="$link"
        else
            PRG=`dirname "$PRG"`"/$link"
        fi
    done
    # cd "`dirname \"$PRG\"`/.." >&-
}

function warn
{
    echo "$*"
}

function die
{
    echo
    echo "$*"
    echo
    exit 1
}

function program_is_installed
{
    # $1 - program to be checked

    which $1 > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo 1
    else
        echo 0
    fi
}

function check_java_cmd
{
    if [ -n "$JAVA_HOME" ] ; then
        if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
            # IBM's JDK on AIX uses strange locations for the executables
            JAVA_CMD="$JAVA_HOME/jre/sh/java"
        else
            JAVA_CMD="$JAVA_HOME/bin/java"
        fi

        if [ ! -x "$JAVA_CMD" ] ; then
            die "Error! invalid JAVA_HOME env path: $JAVA_HOME"
        fi
    else
        JAVA_CMD="java"
        which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
    echo $JAVA_CMD
}
# check_java_cmd

function check_java_version
{
    JAVA_CMD=$1
    JAVA_VERSION=`$JAVA_CMD -version 2>&1 | grep "java version" | awk '{print $3}' | awk -F '.' '{print $2}'`

    if [ -z "$JAVA_VERSION" ]; then
        die "Failed to parse Java version!"
    elif [ $JAVA_VERSION -ne 6 ] && [ $JAVA_VERSION -ne 7 ]; then
        die "ERROR: Java version not supported! Please install Java 6 or 7!"
    fi

    echo $JAVA_VERSION
    exit 0
}
# check_java_version

function check_nodejs
{
    if [ $(program_is_installed node) -eq 0 ]; then
        die "Node.js is not installed, minimum version supported is v0.8.16"
    fi

    echo 1
    exit 0
}
# check_nodejs

function check_nodejs_version
{
    NODE_VER=$(node -v 2>&1)
    MAJOR_VER=$(echo $NODE_VER | awk -F '.' ' { print $2 } ')
    MINOR_VER=$(echo $NODE_VER | awk -F '.' ' { print $3 } ')
    MIN_MAJOR=$MIN_NODE_MAJ_VER
    MIN_MINOR=$MIN_NODE_MAJ_VER

    if [ $MAJOR_VER -lt $MIN_NODE_MAJ_VER ]; then
        die "ERROR: minimum Node.js version supported is $MIN_NODE_VER."
    elif [ $MAJOR_VER -eq $MIN_MAJOR ] && [ $MINOR_VER -lt $MIN_MINOR ]; then
        die "ERROR: The minimum Node.js version supported is $MIN_NODE_VER."
    fi

    echo 1
    exit 0
}
# check_nodejs_version

# # Split up the JVM_OPTS And CDAP_OPTS values into an array, following the shell quoting and substitution rules
# function splitJvmOpts
# {
#     JVM_OPTS=("$@")
# }

# checks if PID already exists. Alert user but still return success
function check_before_start
{
    BASENAME=${1##*/}
    PID_FILE=$PID_DIR/$BASENAME.pid

    # setup $PID_DIR
    if [ ! -d "$PID_DIR" ]; then
        mkdir -p "$PID_DIR"
    fi

    # checks nodejs availability before it starts CDAP
    if [ "$(check_nodejs)" != "1" ] && [ "$(check_nodejs_version)" != "1" ]; then
        die "CDAP requires Node.js! but it's either not installed or not in path. Exiting..."
    fi

    # check existing CDAP processes
    if [ -f $PID_FILE ]; then
        # kill -0 returns 1 if PID exists else 0
        if kill -0 `cat $PID_FILE` > /dev/null 2>&1; then
            echo "$0 running as process `cat $pid`."
            echo "Stop it first or use the restart function"
            exit 0
        fi
    else
        nodejs_pid=`ps | grep web-app/ | grep -v grep | awk ' { print $1 } '`
        if [[ "x{nodejs_pid}" != "x" ]]; then
            kill -9 $nodejs_pid 2>/dev/null >/dev/null
        fi
    fi

    echo "1"
}

# # checks for any updates of standalone
# function check_for_updates
# {
#     # check if connected to internet
#     l=`ping -c 3 $VERSION_HOST 2>/dev/null | grep "64 bytes" | wc -l`
#
#     if [ $l -eq 3 ]; then
#         new=`curl 'http://s3.amazonaws.com/cdap-docs/VERSION' 2>/dev/null`
#         if [[ "x${new}" != "x" ]]; then
#             current=`cat ${APP_HOME}/VERSION`
#             compare_versions $new $current
#             case $? in
#                 0);;
#             1) echo ""
#                 echo "UPDATE: There is a newer version of the CDAP SDK available."
#                 echo "        Download it from http://cask.co/downloads"
#                 echo "";;
#             2);;
#             esac
#         fi
#     fi
# }
#
# function compare_versions
# {
#     if [[ $1 == $2 ]]
#     then
#         return 0
#     fi
#     local IFS=.
#     local i ver1=($1) ver2=($2)
#     # fill empty fields in ver1 with zeros
#     for ((i=${#ver1[@]}; i<${#ver2[@]}; i++))
#     do
#         ver1[i]=0
#     done
#     for ((i=0; i<${#ver1[@]}; i++))
#     do
#         if [[ -z ${ver2[i]} ]]
#         then
#             # fill empty fields in ver2 with zeros
#             ver2[i]=0
#         fi
#         if ((10#${ver1[i]} > 10#${ver2[i]}))
#         then
#             return 1
#         fi
#         if ((10#${ver1[i]} < 10#${ver2[i]}))
#         then
#             return 2
#         fi
#     done
#     return 0
# }
#
# # Rotates the basic start/stop logs
# function rotate_log
# {
#     log=$1;
#     num=5;
#     if [ -n "$2" ]; then
#     num=$2
#     fi
#     if [ -f "$log" ]; then # rotate logs
#     while [ $num -gt 1 ]; do
#         prev=`expr $num - 1`
#         [ -f "$log.$prev" ] && mv -f "$log.$prev" "$log.$num"
#         num=$prev
#     done
#     mv -f "$log" "$log.$num";
#     fi
# }
#
# # Delete the nux file to reenable nux flow
# function reenable_nux
# {
#     rm -f $NUX_FILE
# }
#
# # Checks if this is first time user is using the Standalone CDAP
# function nux_enabled
# {
#     if [ -f $NUX_FILE ];
#     then
#         return 1;
#     else
#         return 0;
#     fi
# }
#
# function nux
# {
#     version=`cat ${APP_HOME}/VERSION`
#     # Deploy apps
#     curl -sL -o /dev/null -H "X-Archive-Name: LogAnalytics.jar" --data-binary "@$APP_HOME/examples/ResponseCodeAnalytics/target/ResponseCodeAnalytics-${version}.jar" -X POST http://127.0.0.1:10000/v2/apps
#     # Start flow and procedure
#     curl -sL -o /dev/null -X POST http://127.0.0.1:10000/v2/apps/ResponseCodeAnalytics/flows/LogAnalyticsFlow/start
#     curl -sL -o /dev/null -X POST http://127.0.0.1:10000/v2/apps/ResponseCodeAnalytics/procedures/StatusCodeProcedure/start
# }
#
# function start
# {
#     debug=$1; shift
#     port=$1; shift
#
#     eval splitJvmOpts $DEFAULT_JVM_OPTS $JAVA_OPTS $CDAP_OPTS
#     check_before_start
#     mkdir -p $APP_HOME/logs
#     rotate_log $APP_HOME/logs/cdap.log
#     rotate_log $APP_HOME/logs/cdap-debug.log
#
#     nohup nice -1 "$JAVACMD" "${JVM_OPTS[@]}" -classpath "$CLASSPATH" co.cask.cdap.StandaloneMain \
#         --web-app-path ${WEB_APP_PATH} \
#         >> $APP_HOME/logs/cdap.log 2>&1 < /dev/null &
#     echo $! > $pid
#
#     check_for_updates
#     echo -n "Starting Standalone CDAP ..."
#
#     background_process=$!
#     while kill -0 $background_process >/dev/null 2>/dev/null ; do
#         if grep '..* started successfully' $APP_HOME/logs/cdap.log > /dev/null 2>&1; then
#             if $debug ; then
#                 echo; echo "Remote debugger agent started on port $port."
#             else
#                 echo
#             fi
#             grep -A 1 '..* started successfully' $APP_HOME/logs/cdap.log
#             break
#         elif grep 'Failed to start server' $APP_HOME/logs/cdap.log > /dev/null 2>&1; then
#             echo; echo "Failed to start server"
#             stop
#             break
#         else
#             echo -n "."
#             sleep 1;
#         fi
#     done
#     echo
#     if ! kill -s 0 $background_process 2>/dev/null >/dev/null; then
#         echo "Failed to start, please check logs for more information."
#     fi
#
#     # Disabling NUX
#     # TODO: Enable NUX with new example, see CDAP-22
#     #nux_enabled
#
#     #NUX_ENABLED=$?
#     #if [ "x$NUX_ENABLED" == "x0" ]; then
#     #  nux
#     #  exit 0;
#     #fi
# }
#
# function stop
# {
#     echo -n "Stopping Standalone CDAP ..."
#     if [ -f $pid ]; then
#         pidToKill=`cat $pid`
#         # kill -0 == see if the PID exists
#         if kill -0 $pidToKill > /dev/null 2>&1; then
#             kill $pidToKill > /dev/null 2>&1
#             while kill -0 $pidToKill > /dev/null 2>&1;
#             do
#                 echo -n "."
#                 sleep 1;
#             done
#             rm $pid
#         else
#             retval=$?
#         fi
#         rm -f $pid
#         echo ""
#         echo "Standalone CDAP stopped successfully."
#     fi
#     echo
# }
#
# function restart
# {
#     stop
#     start $1 $2
# }
#
# function status
# {
#     if [ -f $pid ]; then
#         pidToCheck=`cat $pid`
#         # kill -0 == see if the PID exists
#         if kill -0 $pidToCheck > /dev/null 2>&1; then
#             echo "$0 running as process $pidToCheck"
#             exit 0
#         else
#             echo "pidfile exists, but process does not appear to be running"
#             exit 3
#         fi
#     else
#         echo "$0 is not running"
#         exit 3
#     fi
# }
#
# case "$1" in
#     start|restart)
#         command=$1; shift
#         debug=false
#         nux=false
#
#         while [ $# -gt 0 ]
#         do
#             case "$1" in
#                 --enable-debug) shift; debug=true; port=$1; shift;;
#                 --enable-nux) shift; nux=true;;
#                 *) shift; break;;
#             esac
#         done
#
#         if $nux; then
#             reenable_nux
#         fi
#
#         if $debug ; then
#             shopt -s extglob
#
#             if [ -z "$port" ]; then
#                 port=5005
#             elif [ -n "${port##+([0-9])}" ]; then
#                 die "port number must be an integer.";
#             elif [ $port -lt 1024 ] || [ $port -gt 65535 ]; then
#                 die "port number must be between 1024 and 65535.";
#             fi
#
#             CDAP_OPTS="${CDAP_OPTS} -agentlib:jdwp=transport=dt_socket,address=localhost:$port,server=y,suspend=n"
#         fi
#         $command $debug $port
#         ;;
#
#     stop)
#         $1
#         ;;
#
#     status)
#         $1
#         ;;
#
#     *)
#         echo "Usage: $0 {start|stop|restart|status}"
#         echo "Additional options with start, restart:"
#         echo "--enable-nux  to reenable new user experience flow"
#         echo "--enable-debug [ <port> ] to connect to a debug port for Standalone CDAP (default port is 5005)"
#         exit 1
#         ;;
# esac
# exit $?
#
# VERSION_HOST="205.186.175.189"
