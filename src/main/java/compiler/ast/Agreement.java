package compiler.ast;

import lib.datastructures.Pair;

import java.util.ArrayList;
import java.util.Scanner;

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

    public ArrayList<ArrayList<Pair<Field, String>>> askValues() {
        Scanner input = new Scanner(System.in);
        ArrayList<ArrayList<Pair<Field, String>>> values = new ArrayList<>();
        ArrayList<Pair<Field, String>> str;
        for (Pair<Party, ArrayList<Field>> pair : vals) {
            str = new ArrayList<>();
            System.out.println();
            System.out.println("# Please, " + pair.getFirst().getId() + " insert your id: ");
            String read1 = input.nextLine();

            if (read1.equals(pair.getFirst().getUserId())) {
                System.out.println("# Please, " + pair.getFirst().getId() + " insert the values for the fields: ");
                for (int i = 0; i < pair.getSecond().size(); i++) {
                    System.out.println(pair.getSecond().get(i).getId() + ": ");
                    String read = input.nextLine();
                    str.add(new Pair(pair.getSecond().get(i), read));
                }
                values.add(str);
            } else {
                values = null;
                break;
            }
        }
        for (Party disp : disputers) {
            boolean flag = false;
            for (Pair<Party, ArrayList<Field>> pair : vals) {
                if (pair.getFirst().getId().equals(disp.getId())) {
                    flag = true;
                }
            }
            if (!flag) {
                System.out.println("# Please, " + disp.getId() + " insert your id: ");
                String read1 = input.nextLine();
                if (!read1.equals(disp.getUserId())) {
                    values = null;
                    break;
                }
            }
        }

        return values;
    }

    public boolean doAgree(ArrayList<ArrayList<Pair<Field, String>>> values) {
        boolean agree = true;
        ArrayList<Pair<Field, String>> ref = values.get(0);
        for (int i = 1; i < values.size() && agree; i++) {
            for (int j = 0; j < values.get(i).size(); j++) {
                for (int k = 0; k < ref.size(); k++) {
                    if (ref.get(k).getFirst().getId().equals(values.get(i).get(j).getFirst().getId())) {
                        if (!ref.get(k).getSecond().equals(values.get(i).get(j).getSecond())) {
                            agree = false;
                        }
                    }
                }
            }
        }
        return agree;
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
