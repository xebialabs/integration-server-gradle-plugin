#!/bin/sh
set -e

# first arg is `-f` or `--some-option`
if [ "${1#-}" != "$1" ]; then
	set -- haproxy "$@"
fi

if [ "$1" = 'haproxy' ]; then
	shift
	set -- haproxy -W -db "$@"
fi
sleep 15
export PGPASSWORD=admin
exec "$@"