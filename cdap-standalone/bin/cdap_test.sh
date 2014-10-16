#!/bin/bash
SCRIPT_DIR=$PWD
CHECK_JAVA_VERSION=6
source $SCRIPT_DIR/cdap-standalone/bin/cdap.sh
source $SCRIPT_DIR/cdap-standalone/bin/assert.sh

# TEST FUNCTIONS
function test_set_perm_size
{
    assert set_perm_size "-Xmx1024m -XX:MaxPermSize=128m"
}

function test_script_variables
{
    CHECK="-XX:+UseConcMarkSweepGC -Djava.security.krb5.realm= -Djava.security.krb5.kdc= -Djava.awt.headless=true"
    assert "echo '$CDAP_OPTS'" "$CHECK"
    # assert "echo $WEB_APP_PATH" "web-app/local/server/main.js"
}

function test_warn
{
    assert "warn noooo!" "noooo!"
}

function test_die
{
    assert "die noooo!" "\nnoooo!"
    assert_raises "die noooo!" 1 ""
}

function test_program_is_installed
{
    assert "program_is_installed ls" 1 ""
    assert "program_is_installed abcdef" 0 ""
}

function test_check_java_cmd
{
    # setup
    ORIGINAL_JAVA_HOME=$JAVA_HOME

    # test weird IBM java
    mkdir -p /tmp/jre/sh/
    touch /tmp/jre/sh/java
    chmod +x /tmp/jre/sh/java

    export JAVA_HOME="/tmp"
    assert "echo $(check_java_cmd)" "$JAVA_HOME/jre/sh/java"

    rm -rf /tmp/jre

    # test normal java path
    export JAVA_HOME=$ORIGINAL_JAVA_HOME
    assert "echo $(check_java_cmd)" "$JAVA_HOME/bin/java"

    # test invalid java path
    mkdir -p /tmp/jre/sh/
    touch /tmp/jre/sh/java

    export JAVA_HOME="/tmp"
    assert "echo $(check_java_cmd)" "Error! invalid JAVA_HOME env path: $JAVA_HOME" ""

    rm -rf /tmp/jre

    # test null java home - should return 'java'
    export JAVA_HOME=""
    assert "echo $(check_java_cmd)" "java"

    # tear down
    export JAVA_HOME=$ORIGINAL_JAVA_HOME
}

function test_check_java_version
{
    # pass test
    assert "echo $(check_java_version java)" "$CHECK_JAVA_VERSION"
    assert_raises  "check_java_version java" "0" ""

    # fail test
    assert "echo $(check_java_version not_java)" "Failed to parse Java version!"
    assert_raises  "check_java_version not_java" "1" ""
}

function test_check_nodejs
{
    ORIGINAL_PATH=$PATH

    # pass test
    assert "echo $(check_nodejs)" "1"
    assert_raises  "check_nodejs" "0" ""

    # fail test
    export PATH=""

    assert "echo $(check_nodejs)" \
        "Node.js is not installed, minimum version supported is v0.8.16"
    assert_raises  "check_nodejs" "1" ""

    export PATH=$ORIGINAL_PATH
}

function test_check_nodejs_version
{
    ORIGINAL_PATH=$PATH

    # PASS TEST
    assert "echo $(check_nodejs_version)" "1"
    assert_raises  "check_nodejs_version" "0" ""

    # FAIL TEST
    # create mock nodejs script - returns old version string
    cat <<-"EOF" > /tmp/node
        #!/bin/sh
        if [ -n "$1" ] && [ "$1" == "-v" ]; then
            echo 'v0.8.0'
        fi
EOF
    chmod +x /tmp/node
    ln -s $(which awk) /tmp/awk  # function check_nodejs_version uses awk
    export PATH="/tmp"

    # assert
    assert "echo $(check_nodejs_version)" \
        "ERROR: The minimum Node.js version supported is v0.8.16."
    assert_raises "check_nodejs_version" "1" ""

    # clean up
    export PATH=$ORIGINAL_PATH
    rm /tmp/node 2>/dev/null
    rm /tmp/awk 2>/dev/null
}

function test_check_before_start
{
    # PASS TEST
    assert "echo $(check_before_start cdap)" "1"

    # FAIL TEST - OLD NODE VERSION
    # create mock nodejs script - returns old version string
    cat <<-"EOF" > /tmp/node
        #!/bin/sh
        if [ -n "$1" ] && [ "$1" == "-v" ]; then
            echo 'v0.8.0'
        fi
EOF
    chmod +x /tmp/node
    ln -fs $(which awk) /tmp/awk  # function check_nodejs_version uses awk
    ln -fs $(which ps) /tmp/ps  # function check_before_start uses ps
    ln -fs $(which grep) /tmp/grep  # function check_before_start uses grep
    ln -fs $(which sed) /tmp/sed  # assert needs sed
    ln -fs $(which bc) /tmp/bc  # assert needs sed
    ln -fs $(which date) /tmp/date  # assert needs sed
    export PATH="/tmp"

    # assert fail on old version
    assert "check_before_start" \
        "\nCDAP requires Node.js! but it's either not installed or not in path. Exiting..."

    # assert fail on non-existent node
    rm /tmp/node 2>/dev/null
    assert "check_before_start" \
        "\nCDAP requires Node.js! but it's either not installed or not in path. Exiting..."

    # clean up
    export PATH=$ORIGINAL_PATH
    rm /tmp/awk 2>/dev/null
    rm /tmp/ps 2>/dev/null
    rm /tmp/grep 2>/dev/null
    rm /tmp/sed 2>/dev/null
}

function test_compare_versions
{
    assert "compare_versions '1.0.0' '1.0.0'" "0"
    assert "compare_versions '2.0.0' '1.0.0'" "1"
    assert "compare_versions '1.0.0' '2.0.0'" "2"
}

function test_check_for_updates
{
    # PASS TEST
    echo "2.5.0" > $APP_HOME/VERSION
    assert "check_for_updates" ""

    # FAIL TEST - PROMPT USER TO UPDATE
    echo "1.5.0" > $APP_HOME/VERSION
    assert "check_for_updates" \
        "\nUPDATE: There is a newer version of the CDAP SDK available.\nDownload it from http://cask.co/downloads"

    # FAIL TEST - HAVE NEWER VERSION THAN CASK - IS THAT POSSIBLE?
    echo "100.0.0" > $APP_HOME/VERSION
    assert "check_for_updates" ""

    rm $APP_HOME/VERSION
}


# TESTS
test_set_perm_size
test_script_variables
test_warn
test_die
test_program_is_installed
test_check_java_cmd
test_check_java_version
test_check_nodejs
test_check_nodejs_version
test_check_before_start
test_compare_versions
test_check_for_updates

assert_end regression
echo "Done!"
