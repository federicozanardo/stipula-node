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
