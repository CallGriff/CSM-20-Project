
# CS-M20 Project: Blockchain-Based Bank Management System

This project's contents contains the web-app source code for the system, along with the smart contract used to run with the Quorum blockchain. 

# Pre-requisites

* Operating system: Ubuntu v22.04

* Ideally >8GB RAM

* [Go version 1.21.1](https://go.dev/doc/install)

* [Geth version 1.9.6-unstable](https://geth.ethereum.org/docs/getting-started/installing-geth)

* [Node.js and NPM](https://nodejs.org/en/download/package-manager)

* [Docker-Compose (From docker-desktop)](https://docs.docker.com/desktop/install/ubuntu/)

# Run the blockchain 

Contained within this directory are the 8-node configurations for the QBFT, raft and clique blockchains, ready to use. Open a terminal within one of these directories, then run the following commands:

```
docker-compose config

docker-compose up -d
```

This will create a new container for the blockchain and run each node. To verify the nodes have established a connection, type the following commands in the same console:

```
geth attach 127.0.0.1:22000 // brings up a javascript console for the selected node

net.peerCount //should 7
```

Each node uses a specific port. This can be modified in both the static-nodes.json and docker-compose file if there are any existing conflicts.

Additionally, the logs for a running node can be viewed by executing the following console command:

```
docker-compose logs node0 // returns logs for node0
```

# Run the application

To run the application, ensure you have a running blockchain by following the above instruction. Then, the application can be run by executing the 'Web3App-0.1.0-all.jar' file. Running this file for the first time will deploy the application's smart contract and generate a contractaddress.txt file, and bring up the user interface.

# Benchmarking the blockchain

The benchmarks for the blockchain were carried out using tests on [jmeter](https://jmeter.apache.org/usermanual/get-started.html). The test file used in the project can be found in this directory (.jmx file). This test runs a series of HTTP requests at the address of the first node (node0), simulating a user on the system. The number of users (threads) can be modified in the thread group section.

To run the test file, ensure there is a running blockchain and the bank contract has been deployed as described above and place the .jmx file in the bin folder in the jmeter directory. Then, open a console within the jmeter bin directory and enter the following command:

```
jmeter -n -t ./HTTP-Request.jmx -l ./results/results.csv
```

This command also stores the results in CSV-format in a results.csv file.

# Shut-down the blockchain

Finally, to bring down the blockchain, type the following command in the terminal:

```
docker-compose down
```

Then, if you're using docker-desktop, you can manually delete the generated images of the nodes, this can be used to remove the blockchain from docker, or you can bring up a new blockchain by re-running the 'docker-compose up -d' command in console.