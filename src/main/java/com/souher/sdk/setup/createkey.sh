cd /java/ssl
cp -rf *.crt a.crt
cp -rf *.key a.key

openssl pkcs12 -export -out ssl.pfx -in a.crt -inkey a.key

keytool -importkeystore -srckeystore ssl.pfx -destkeystore a.jks -srcstoretype PKCS12 -deststoretype JKS
