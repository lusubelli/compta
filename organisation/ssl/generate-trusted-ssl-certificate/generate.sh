#!bin/bash

openssl req \
    -newkey rsa:2048 \
    -x509 \
    -nodes \
    -keyout server.key \
    -new \
    -out server.crt \
    -config ./generate-trusted-ssl-certificate/openssl-custom.cnf \
    -sha256 \
    -days 365

$JAVA_HOME\\bin\\keytool.exe -genkeypair -alias localhost -keyalg RSA -keysize 2048 -keystore keystore.jks -validity 3650 -storepass password
$JAVA_HOME\\bin\\keytool.exe -list -v -keystore keystore.jks

openssl pkcs12 \
    -export \
    -name localhost \
    -in server.crt \
    -inkey server.key \
    -out localhost.p12

$JAVA_HOME\\bin\\keytool.exe -importkeystore -destkeystore localhost.keystore -srckeystore localhost.p12 -srcstoretype pkcs12 -alias localhost
