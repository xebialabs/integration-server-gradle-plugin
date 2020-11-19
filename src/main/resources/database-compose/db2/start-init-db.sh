#!/usr/bin/env bash
set -e

if [ -f "/home/dbtmp" ]; then
    echo -e "$DB2INST1_PASSWORD\n$DB2INST1_PASSWORD" | passwd db2inst1
fi

if [ -f "/home/dbtmp" ]; then
    echo "Initializing database ..."

    useradd -M -p xldrepo xldrepo

    echo -e "xldrepo\nxldrepo" | passwd xldrepo

    su - db2inst1 -c "db2 -stvf /create_xldrepo.sql"
    echo "Done initializing database"
fi

rm -f /home/dbtmp

su - db2inst1 -c "db2 connect to xldrepo"
