#!/usr/bin/env sh

if test -f /secrets/serviceuser/srvsaf/username;
then
    echo "Setting serviceuser_username"
    export  serviceuser_username=$(cat /secrets/serviceuser/srvsaf/username)
fi
if test -f /secrets/serviceuser/srvsaf/password;
then
    echo "Setting serviceuser_password"
    export  serviceuser_password=$(cat /secrets/serviceuser/srvsaf/password)
fi
if test -f /var/run/secrets/nais.io/vault/client_id;
then
    echo "Setting Azure ClientId"
    export  azure_app_clientId=$(cat /var/run/secrets/nais.io/vault/client_id)
fi
if test -f /var/run/secrets/nais.io/vault/client_secret;
then
    echo "Setting Azure clientSecret"
    export  azure_app_clientSecret=$(cat /var/run/secrets/nais.io/vault/client_secret)
fi
