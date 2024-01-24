#!/bin/bash
#
# Copyright (c) 2024 Oracle and/or its affiliates.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

echo 'Generate new key store...'
keytool -genkeypair -keyalg RSA -keysize 2048 -alias service_cert -dname "CN=security.j4c,O=Oracle,L=Prague,ST=Some-State,C=CZ" -validity 21650 -keystore keystore.p12 -storepass changeit -keypass changeit -deststoretype pkcs12
echo 'Obtaining certificate...'
keytool -exportcert -keystore keystore.p12 -storepass changeit -alias service_cert -rfc -file service_cert.cer

echo 'Adding single private key to the keystore...'
openssl pkcs12 -in keystore.p12 -nodes -out keystore-private.key -nocerts -passin pass:changeit
openssl pkcs12 -inkey keystore-private.key -export -out keystore.p12 -name myprivatekey -passin pass:changeit -passout pass:changeit -nocerts

echo 'Adding self-signed certificate to the keystore...'
keytool  -importcert  -alias service_cert -file service_cert.cer -keystore keystore.p12 -storepass changeit -noprompt

echo 'Cleaning key and cer files'
rm keystore-private.key  service_cert.cer


