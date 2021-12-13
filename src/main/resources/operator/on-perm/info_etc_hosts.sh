#!/bin/bash

MY_PATH=$(dirname "$0")
MY_PATH=$(cd "$MY_PATH" && pwd)
MY_NAME=$(whoami)

echo "Please enter your password if requested for user ${MY_NAME} or give user sudoers permissions '${MY_NAME} ALL=(ALL) NOPASSWD: ${MY_PATH}/update_etc_hosts.sh'."
