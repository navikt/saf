#!/usr/bin/env sh

if test -f /secrets/serviceuser/srvsaf/username;
then
    echo "Setting serviceuser_username"
    export  serviceuser_username=$(cat /secrets/serviceuser/srvsaf/username)
fi
if test -f /secrets/serviceuser/srvsaf/password;
then
    echo "Setting serviceuser_password"
    export  serviceuser_***passord=gammelt_passord***)
fi
