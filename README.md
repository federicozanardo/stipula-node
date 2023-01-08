# Stipula Node

What does it include?
- [ ] Message layer (API)
  - [ ] Handle the socket 
  - [ ] Handle the messages
- [ ] Execution layer
  - [ ] Stipula Virtual Machine (SVM)
    - [ ] Smart Contract Virtual Machine
    - [ ] Script Virtual Machine
  - [ ] Handle triggers
- [ ] Deploy a contract
  - [ ] Stipula compiler 
- [ ] Consensus layer
- [ ] Commitment layer

## Status of the project

The node is able to:
- [ ] Call functions
- [ ] Keep the state of the contract
- [ ] Manage addresses
- [ ] Manage assets
- [ ] Create and manage a contract instances

## How to run a node?

Build the image:
```
docker run stipula-node:<version>
```

Example:
```bash
docker run stipula-node:v0.1.5
```

Run the image:
```
docker run -d stipula-node:<version>
```

Example
```bash
docker run -d stipula-node:v0.1.5
```