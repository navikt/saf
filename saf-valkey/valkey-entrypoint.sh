#!/bin/bash

exec valkey-server /app/valkey.conf --requirepass $VALKEY_PASSWORD
