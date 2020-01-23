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
if test -d /var/run/secrets/nais.io/vault;
then
    echo "Setting no_nav_security_jwt_issuer_azurev1_acceptedAudience"
    export  no_nav_security_jwt_issuer_azurev1_acceptedAudience=$(cat /var/run/secrets/nais.io/vault/no_nav_security_jwt_issuer_azurev1_acceptedAudience)
    echo "Setting no_nav_security_jwt_issuer_azurev2_acceptedAudience"
    export  no_nav_security_jwt_issuer_azurev2_acceptedAudience=$(cat /var/run/secrets/nais.io/vault/no_nav_security_jwt_issuer_azurev2_acceptedAudience)
    echo "Setting no_nav_security_jwt_issuer_openam_acceptedAudience"
    export  no_nav_security_jwt_issuer_openam_acceptedAudience=$(cat /var/run/secrets/nais.io/vault/no_nav_security_jwt_issuer_openam_acceptedAudience)
    echo "Setting no_nav_security_jwt_issuer_reststs_acceptedAudience"
    export  no_nav_security_jwt_issuer_reststs_acceptedAudience=$(cat /var/run/secrets/nais.io/vault/no_nav_security_jwt_issuer_reststs_acceptedAudience)
fi
