#!/bin/bash

IP="43.216.132.89"
PORT="9091"
PACKAGE="octopus3-repository-service-1.0-SNAPSHOT-all.jar"
NAME="octopus3-repository-service"
LOGDIR="/opt/logs/repository"
ENVIRONMENT="dev"

if cd "`dirname \"$0\"`"; then
    CURRENT_PATH=`pwd`
    cd "$OLDPWD" || exit 1
else
    exit 1
fi

DEPLOY_SCRIPT_PATH="${CURRENT_PATH}/../../octopus3-commons/scripts/"
DEPLOY_SCRIPT="deploy.sh"
PACKAGE_PATH="${CURRENT_PATH}/../build/libs/"
USERNAME="GWTSYN_dev_rw"

eval ${DEPLOY_SCRIPT_PATH}${DEPLOY_SCRIPT} -i ${IP} -u ${USERNAME} -p ${PORT} -j ${PACKAGE_PATH}${PACKAGE} -l ${LOGDIR} -e ${ENVIRONMENT} -n ${NAME} $@
