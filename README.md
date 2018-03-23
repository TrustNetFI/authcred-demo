# Verifiable credentials and delegated authorization demo
## 1. Introduction
This is a quick and  dirty demo on using W3C type Verifiable Credentials for delegating resource access build on top of Hyperledger Indy. 

## 2. Prerequisites and setup

Indy 1.3.1 SDK & Java wrapper

Build Verifiable Credentials https://github.com/TrustNetFI/verifiable-credentials-java 

In project ```setupindy``` open ```setup.java ``` and modify  ```pool_name ```,  ```TRUSTEE_DID ``` and  ```TRUSTEE_SEED ```   to meet your Indy setup.


If you use other pool than  ```default_pool ``` you need to change the  ```NETWORK_NAME in  ```  ```IndyConfig.java ``` files in  ```resource-server ```,  ```agent ``` and  ```demo-client ``` projects.


Run  ```setupindy ```. This creates needed wallets and keys. 


## 3. Running the demo
+ Start projects resource-server, agent and demo-client.
+ Go to ``` http://localhost:8080``` . Username is  ```alice``` and password is ``password``` .
+ Accept the connection request, you will be redirected to resource server, login using username ```alice``` and password ```password```. Accept the connection and you will be redirected back to the agent.
+ Accept the credential offer from the Company X. 
+ Issue new delegated access credential to service Y.
+ Go to service example-client ```http://localhost:8099```
+ Request the access token, should receive access token 
+ Request the data, should receive sample data
+ On agent side  ```(http://localhost:8080) ``` click revoke 
+ On example client side try to make new data request, should receive UNAUTHORIZED

 ## 4. Troubleshooting
 If you receive JNA errors add
~~~~
 <jna.version>4.2.1</jna.version>
 <dependency>
          <groupId>net.java.dev.jna</groupId>
          <artifactId>jna</artifactId>
          <version>${jna.version}</version>
      </dependency>
~~~~
to all  ```pom.xml ``` files.

 

## 5. Resources

[Indy](https://www.hyperledger.org/projects)

[Verifiable Credentials implementation](]https://github.com/TrustNetFI/verifiable-credentials-java)

[W3C Verifiable Credentials](https://www.w3.org/2017/vc/WG/)
