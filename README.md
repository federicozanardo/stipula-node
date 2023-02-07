# Stipula Node

What does it include?
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
- [ ] Deploy a contract
  - [x] Handle the request 
  - [ ] Stipula compiler 
- [ ] Consensus layer
- [ ] Commitment layer

## Status of the project

The node is able to:
- [x] Call functions
- [x] Keep the state of the contract
- [x] Manage addresses
- [x] Manage assets
- [x] Create and manage a contract instances
- [x] Manage storage
- [x] Add Docker support
- [x] Manage event trigger

## How to run a node?

Build the image:
```
docker build -t stipula-node:<version> .
```

Example:
```bash
docker build -t stipula-node:v0.1.5 .
```

Run the image:
```
docker run -d stipula-node:<version>
```

Example
```bash
docker run -d stipula-node:v0.1.5
```

In order to execute properly the node, there is the need to launch it with `docker-compose`:
```
docker-compose -f docker-compose.yml up -d
```

Inside the `docker-compose.yml` there is the need to specify the version of the image (i.e. `image: stipula-node:v0.2.0`). Furthermore, due to the limitations in the current development, in order to start up the node, there is the need to *seed* the node with ownerships (single-use seals) and assets (create an asset an its supply). In order to do that, there is the need to specify the seeding process with `yes` or `no`:
```
environment:
  - SEED=no
```
