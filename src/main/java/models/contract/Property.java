package models.contract;

import java.io.Serializable;

public class Property implements Serializable {
    private final String id;
    private final SingleUseSeal singleUseSeal;
    private String unlockScript;
    private String contractInstance;

    public Property(String id, SingleUseSeal singleUseSeal) {
        this.id = id;
        this.singleUseSeal = singleUseSeal;
        this.unlockScript = "";
        this.contractInstance = "";
    }

    public String getId() {
        return id;
    }

    public SingleUseSeal getSingleUseSeal() {
        return singleUseSeal;
    }

    public String getUnlockScript() {
        return unlockScript;
    }

    public String getContractInstance() {
        return contractInstance;
    }

    public void setUnlockScript(String unlockScript) {
        this.unlockScript = unlockScript;
    }

    public void setContractInstance(String contractInstance) {
        this.contractInstance = contractInstance;
    }

    @Override
    public String toString() {
        return "Property{" +
                "id='" + id + '\'' +
                ", singleUseSeal=" + singleUseSeal +
                '}';
    }
}
