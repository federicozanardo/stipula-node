package models.dto.requests.contract.deploy;

import models.dto.requests.Message;

public class DeployContract extends Message {
    public DeployContract() {
        super(DeployContract.class.getSimpleName());
    }
}

