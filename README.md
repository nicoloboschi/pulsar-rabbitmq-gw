# Pulsar RabbitMQ gateway

The Pulsar RabbitMQ gateway acts as a bridge between your RabbitMQ application and Apache Pulsar.
It implements the AMQP 0.9.1 protocol used by RabbitMQ clients and translates AMQP frames and concepts to Pulsar ones.
The gateway can be run as a standalone jar, a Pulsar 
[Pluggable Protocol Handler](https://github.com/apache/pulsar/wiki/PIP-41%3A-Pluggable-Protocol-Handler) 
 or a Pulsar [Proxy extension](https://github.com/apache/pulsar/wiki/PIP-99%3A-Pulsar-Proxy-Extensions).

## Limitations

This is currently not implemented but on the roadmap:
* Topic and headers exchanges
* Exclusive consumers
* Non durable exchanges and queues
* Transient messages (all messages are persisted)

RabbitMQ and Pulsar work in a pretty different way. The Pulsar RabbitMQ gateway was designed to make the most benefit 
from Pulsar's scalability. This results in some differences of behavior:
* Canceling an AMQP consumer will requeue the messages that were received through it since it also closes
the associated Pulsar consumers.

## Get started

### Download and Build the Pulsar RabbitMQ gateway

To build from code, complete the following steps:
1. Clone the project from GitHub.

```bash
git clone https://github.com/datastax/pulsar-rabbitmq-gw.git
cd pulsar-rabbitmq-gw
```

2. Build the project.
```bash
mvn clean install -DskipTests
```

You can find the executable jar file in the following directory.
```bash
./pulsar-rabbitmq-gw/target/pulsar-rabbitmq-gw-${version}-jar-with-dependencies.jar
```
You can find the nar file in the following directory.
```bash
./pulsar-rabbitmq-gw/target/pulsar-rabbitmq-gw-${version}.nar
```

### Running Pulsar RabbitMQ as a standalone executable jar

1. Set the URLs of the Pulsar brokers and the ZooKeeper configuration store in a configuration file. Eg:
   ```properties
   brokerServiceURL=pulsar://localhost:6650
   brokerWebServiceURL=http://localhost:8080
   configurationStoreServers=localhost:2181
   ```
2. Run as a Java application and provide the configuration file path in the `-c/--config` option:
   ```bash
   java -jar ./pulsar-rabbitmq-gw/target/pulsar-rabbitmq-gw-${version}-jar-with-dependencies.jar -c conf/gateway.conf
   ```

#### Running Pulsar RabbitMQ as a protocol handler

The Pulsar RabbitMQ gateway can be embedded directly into the Pulsar brokers by loading it as a protocol handler.

1. Set the configuration of the Pulsar RabbitMQ gateway protocol handler in the broker configuration file (generally `broker.conf` or `standalone.conf`).
   Example where the NAR file was copied into the `./protocols` directory:
    ```properties
   messagingProtocols=rabbitmq
   protocolHandlerDirectory=./protocols
    ```

2. Set the AMQP service listeners. Note that the hostname value in listeners is the same as Pulsar broker's `advertisedAddress`. 
   The following is an example.
   ```properties
   amqpListeners=amqp://127.0.0.1:5672
   advertisedAddress=127.0.0.1
   ```

3. Start the Pulsar broker

### Running Pulsar RabbitMQ as a proxy extension

The Pulsar RabbitMQ gateway can be embedded into the Pulsar proxy by loading it as a proxy extension.

1. Set the configuration of the Pulsar RabbitMQ gateway protocol handler in the broker configuration file (generally `broker.conf` or `standalone.conf`).
   Example where the NAR file was copied into the `./protocols` directory:
    ```properties
   proxyExtensions=rabbitmq
   proxyExtensionsDirectory=./protocols
    ```

2. Set the AMQP service listeners. Note that the hostname value in listeners is the same as Pulsar broker's `advertisedAddress`.
   The following is an example.
   ```properties
   amqpListeners=amqp://127.0.0.1:5672
   advertisedAddress=127.0.0.1
   ```

3. Start the Pulsar proxy

## Configuration

### Generic configuration
|Name|Description|Default|
|---|---|---|
|configurationStoreServers|Zookeeper configuration store connection string (as a comma-separated list)|
|amqpListeners|Used to specify multiple advertised listeners for the gateway. The value must format as `amqp[s]://<host>:<port>`, multiple listeners should be separated with commas.|amqp://127.0.0.1:5672
|amqpSessionCountLimit|The maximum number of sessions which can exist concurrently on an AMQP connection.|256
|amqpHeartbeatDelay|The default period with which Broker and client will exchange heartbeat messages (in seconds) when using AMQP. Clients may negotiate a different heartbeat frequency or disable it altogether.|0
|amqpHeartbeatTimeoutFactor|Factor to determine the maximum length of that may elapse between heartbeats being received from the peer before an AMQP0.9 connection is deemed to have been broken.|2
|amqpNetworkBufferSize|AMQP Network buffer size.|2097152 (2MB)
|amqpMaxMessageSize|AMQP Max message size.|104857600 (100MB)
|amqpDebugBinaryDataLength|AMQP Length of binary data sent to debug log.|80
|amqpConnectionCloseTimeout|Timeout in ms after which the AMQP connection closes even if a ConnectionCloseOk frame is not received|2000
|amqpBatchingEnabled|Whether batching messages is enabled in AMQP|true

### Gateway authentication configuration
|Name|Description|Default|
|---|---|---|
|authenticationEnabled| Whether authentication is enabled for the gateway  |false|
|amqpAuthenticationMechanisms|Authentication mechanism name list for AMQP (a comma-separated list of mecanisms. Eg: PLAIN,EXTERNAL)|PLAIN
|tokenSecretKey| Configure the secret key to be used to validate auth tokens. The key can be specified like: `tokenSecretKey=data:;base64,xxxxxxxxx` or `tokenSecretKey=file:///my/secret.key`.  Note: key file must be DER-encoded.||
|tokenPublicKey| Configure the public key to be used to validate auth tokens. The key can be specified like: `tokenPublicKey=data:;base64,xxxxxxxxx` or `tokenPublicKey=file:///my/secret.key`. Note: key file must be DER-encoded.||
|tokenAuthClaim| Specify the token claim that will be used as the authentication "principal" or "role". The "subject" field will be used if this is left blank ||
|tokenAudienceClaim| The token audience "claim" name, e.g. "aud". It is used to get the audience from token. If it is not set, the audience is not verified. ||
|tokenAudience| The token audience stands for this broker. The field `tokenAudienceClaim` of a valid token need contains this parameter.| |

### Broker client configuration
|Name|Description|Default|
|---|---|---|
|brokerServiceURL| The service URL pointing to the broker cluster. | |
|brokerWebServiceURL| The Web service URL pointing to the broker cluster | |
|brokerClientAuthenticationPlugin|  The authentication plugin used by the Pulsar proxy to authenticate with Pulsar brokers  ||
|brokerClientAuthenticationParameters|  The authentication parameters used by the Pulsar proxy to authenticate with Pulsar brokers  ||
|amqpBrokerClientAuthenticationParameters|If set, the RabbitMQ service will use these parameters to authenticate on Pulsar's brokers. If not set, the brokerClientAuthenticationParameters setting will be used. This setting allows to have different credentials for the proxy and for the RabbitMQ service|
|tlsEnabledWithBroker|  Whether TLS is enabled when communicating with Pulsar brokers. |false|
|brokerClientTrustCertsFilePath|  The path to trusted certificates used by the Pulsar proxy to authenticate with Pulsar brokers ||
|brokerClientTlsEnabledWithKeyStore| Whether the gateway use KeyStore type to authenticate with Pulsar brokers ||
|brokerClientTlsTrustStoreType| TLS TrustStore type configuration for gateway: JKS, PKCS12  used by the gateway to authenticate with Pulsar brokers ||
|brokerClientTlsTrustStore| TLS TrustStore path for proxy,  used by the Pulsar proxy to authenticate with Pulsar brokers ||
|brokerClientTlsTrustStorePassword| TLS TrustStore password for proxy,  used by the Pulsar proxy to authenticate with Pulsar brokers ||

### Gateway TLS configuration
|Name|Description|Default|
|---|---|---|
|tlsCertRefreshCheckDurationSec| TLS certificate refresh duration in seconds. If the value is set 0, check TLS certificate every new connection. | 300 |
|tlsCertificateFilePath|  Path for the TLS certificate file ||
|tlsKeyFilePath|  Path for the TLS private key file ||
|tlsTrustCertsFilePath| Path for the trusted TLS certificate pem file ||
|tlsAllowInsecureConnection| Accept untrusted TLS certificate from client. If true, a client with a cert which cannot be verified with the `tlsTrustCertsFilePath` cert will be allowed to connect to the server, though the cert will not be used for client authentication ||
|tlsHostnameVerificationEnabled|  Whether the hostname is validated when the proxy creates a TLS connection with brokers  |false|
|tlsRequireTrustedClientCertOnConnect|  Whether client certificates are required for TLS. Connections are rejected if the client certificate isn’t trusted. |false|
|tlsProtocols|Specify the tls protocols the broker will use to negotiate during TLS Handshake. Multiple values can be specified, separated by commas. Example:- ```TLSv1.3```, ```TLSv1.2``` ||
|tlsCiphers|Specify the tls cipher the broker will use to negotiate during TLS Handshake. Multiple values can be specified, separated by commas. Example:- ```TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256```||
|tlsRequireTrustedClientCertOnConnect| Whether client certificates are required for TLS. Connections are rejected if the client certificate isn't trusted ||
|tlsEnabledWithKeyStore| Enable TLS with KeyStore type configuration for gateway ||
|tlsProvider| TLS Provider ||
|tlsKeyStoreType| TLS KeyStore type configuration for gateway: JKS, PKCS12 ||
|tlsKeyStore| TLS KeyStore path for gateway ||
|tlsKeyStorePassword| TLS KeyStore password for gateway ||
|tlsTrustStoreType| TLS TrustStore type configuration for proxy: JKS, PKCS12 ||
|tlsTrustStore| TLS TrustStore path for gateway ||
|tlsTrustStorePassword| TLS TrustStore password for proxy ||
