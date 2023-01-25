package models.contract;

import java.io.Serializable;

public class Property implements Serializable {
    private final String id;
    private final SingleUseSeal singleUseSeal;

    public Property(String id, SingleUseSeal singleUseSeal) {
        this.id = id;
        this.singleUseSeal = singleUseSeal;
    }

    public String getId() {
        return id;
    }

    public SingleUseSeal getSingleUseSeal() {
        return singleUseSeal;
    }

    @Override
    public String toString() {
        return "Property{" +
                "id='" + id + '\'' +
                ", singleUseSeal=" + singleUseSeal +
                '}';
    }
}
