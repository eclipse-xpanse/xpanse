#!/bin/sh

# Generate RSA private key if not already generated
if [ ! -f "/home/xpanse/private_key.pem" ]; then
    openssl genpkey -algorithm RSA -out /home/xpanse/private_key.pem
fi

# Start xpanse-runtime application
java -jar xpanse-runtime.jar
