package compiler.ast;

import lib.datastructures.Pair;

import java.util.ArrayList;

public class Agreement {
    public static final int LEN = 5;
    private final ArrayList<Party> disputers;
    private final ArrayList<Field> vars;
    private ArrayList<Pair<Party, ArrayList<Field>>> vals = null;

    public Agreement(ArrayList<Party> d, ArrayList<Field> v) {
        disputers = d;
        vars = v;

        for (Party disp : disputers) {
            disp.setUserId(generateUserId(LEN).toString());
        }
    }

    public Agreement(ArrayList<Party> d, ArrayList<Field> v, ArrayList<Pair<Party, ArrayList<Field>>> l) {
        disputers = d;
        vars = v;
        vals = l;

        for (Party disp : disputers) {
            disp.setUserId(generateUserId(LEN).toString());
        }

        for (Pair<Party, ArrayList<Field>> pair : vals) {
            for (Party disp : disputers) {
                if (disp.getId().equals(pair.getFirst().getId())) {
                    pair.getFirst().setUserId(disp.getUserId());
                }
            }

        }

    }

    public ArrayList<Party> getParties() {
        return disputers;
    }

    public StringBuilder generateUserId(int len) {
        String AlphaNumericStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz0123456789";
        StringBuilder s = new StringBuilder(len);
        int i;
        for (i = 0; i < len; i++) {
            int ch = (int) (AlphaNumericStr.length() * Math.random());
            s.append(AlphaNumericStr.charAt(ch));
        }
        return s;
    }

    public void printAgreement() {
        for (Party d : disputers) {
            d.printParty();
        }
        for (Field f : vars) {
            f.printField();
        }
    }
}
