D:\sdk\jdk-12.0.1\bin\keytool.exe -genkeypair -alias localhost -keyalg RSA -keysize 2048 -keystore keystore.jks -validity 3650 -storepass password
D:\sdk\jdk-12.0.1\bin\keytool.exe -list -v -keystore keystore.jks
openssl pkcs12 -export -name localhost -in server.crt -inkey server.key -out localhost.p12
D:\sdk\jdk-12.0.1\bin\keytool.exe -importkeystore -destkeystore localhost.keystore -srckeystore localhost.p12 -srcstoretype pkcs12 -alias localhost
