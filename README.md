# Stipula Node

What does it include at the moment?
- [x] Message layer (API)
  - [x] Handle the socket 
  - [x] Handle the messages
- [x] Execution layer
  - [x] Stipula Virtual Machine (SVM)
    - [x] Smart Contract Virtual Machine
    - [x] Script Virtual Machine
  - [x] Handle triggers
    - [x] Thread management
    - [x] Develop code for virtual machine
- [x] Deploy a contract
  - [x] Handle the request 
  - [x] Stipula compiler 

Future improvements:
- [ ] Consensus layer (see https://github.com/federicozanardo/stipula-node/milestone/3)
- [ ] Commitment layer (see https://github.com/federicozanardo/stipula-node/milestone/3)

## Status of the project

The node is able to:
- [x] Create and manage a contract instances
- [x] Keep the state of the contract
- [x] Call functions
- [x] Manage parties (addresses)
- [x] Manage assets
- [x] Manage storage
- [x] Schedule and trigger events
- [x] Manage payments ("pay-to-contract" and "pay-to-party")
- [x] Manage client connections and requests/responses 
- [x] Add Docker support
- [x] Compiler contracts
- [x] Dynamic typing

"Pay-to-Contract" means when a user wants to pay a contract and "Pay-to-Party" means when the contract wants to pay a party. 

## How to run a node?

Build the image:
```
docker build -t stipula-node:<version> .
```

Example:
```bash
docker build -t stipula-node:v0.4.2 .
```

Run the image:
```
docker run -d stipula-node:<version>
```

Example
```bash
docker run -d stipula-node:v0.4.2
```

In order to execute properly the node, there is the need to launch it with `docker-compose`:
```
docker-compose -f docker-compose.yml up -d
```

Inside the `docker-compose.yml` there is the need to specify the version of the image (i.e. `image: stipula-node:v0.4.2`). Furthermore, due to the limitations in the current development, in order to start up the node, there is the need to *seed* the node with ownerships (single-use seals) and assets (create an asset an its supply). In order to do that, there is the need to specify the seeding process with `yes` or `no`:
```
environment:
  - SEED=no
```
Another way to run a node is to substitute 
```
image: stipula-node:v0.4.2
```
with
```
image: "ghcr.io/federicozanardo/stipula-node:v0.4.2"
```
In this way, `docker-compose` downloads the image from GitHub packages section and runs a node.

## Generate the grammar

Download the `jar` file from https://github.com/antlr/website-antlr4/blob/gh-pages/download/antlr-4.10-complete.jar.

```bash
java -jar antlr-4.10-complete.jar -visitor Stipula.g4
```

## Example of client

An example of an application interacting with the current Stipula implementation is available at https://github.com/federicozanardo/stipula-client.

## Guide

The functioning of the architecture is explained in the thesis present at the address https://github.com/federicozanardo/stipula-thesis.
