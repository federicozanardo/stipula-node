package compiler.ast;

import compiler.parser.StipulaBaseVisitor;
import compiler.parser.StipulaParser;
import lib.datastructures.Pair;
import lib.datastructures.Triple;
import vm.dfa.states.DfaState;
import vm.dfa.states.FinalStates;
import vm.dfa.transitions.ContractCallByEvent;
import vm.dfa.transitions.ContractCallByParty;
import vm.dfa.transitions.TransitionData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class StipulaCompiler extends StipulaBaseVisitor {
    private final ArrayList<String> parties;
    private final Map<Pair<String, Integer>, Type> globalVariables;
    private final Map<String, ArrayList<String>> functionTypes;
    private final ArrayList<String> obligationFunctions;
    private String finalBytecode;
    private DfaState initialState;
    private final HashSet<DfaState> acceptanceStates;
    private final HashSet<DfaState> failingStates;
    private final ArrayList<Triple<DfaState, DfaState, TransitionData>> transitions;

    public StipulaCompiler(Map<Pair<String, Integer>, Type> globalVariables, Map<String, ArrayList<String>> functionTypes) {
        this.parties = new ArrayList<>();
        this.globalVariables = globalVariables;
        this.functionTypes = functionTypes;
        this.obligationFunctions = new ArrayList<>();
        this.acceptanceStates = new HashSet<>();
        this.failingStates = new HashSet<>();
        this.transitions = new ArrayList<>();
    }

    public DfaState getInitialState() {
        return initialState;
    }

    public FinalStates getFinalStates() {
        return new FinalStates(acceptanceStates, failingStates);
    }

    public ArrayList<Triple<DfaState, DfaState, TransitionData>> getTransitions() {
        return transitions;
    }

    @Override
    public String visitProg(StipulaParser.ProgContext context) {
        if (context.agreement() != null) {
            initialState = new DfaState(context.init_state.getText());
            finalBytecode = fullVisitAgreement(context.agreement(), context.init_state.getText());
        }

        for (StipulaParser.FunContext functionContext : context.fun()) {
            StipulaContract cnt = visitFun(functionContext);
        }

        for (String obligationFunction : obligationFunctions) {
            finalBytecode += obligationFunction;
        }

        return finalBytecode;
    }

    public String fullVisitAgreement(StipulaParser.AgreementContext context, String initialState) {
        String methodSignature = "fn agreement ";

        for (StipulaParser.PartyContext party : context.party()) {
            methodSignature += party.getText() + ",";
        }
        methodSignature = methodSignature.substring(0, methodSignature.length() - 1) + " ";
        methodSignature += initialState + " ";

        for (int i = 0; i < context.vardec().size(); i++) {
            String v = context.vardec().get(i).getText();

            for (Pair<String, Integer> globalVariable : globalVariables.keySet()) {
                if (globalVariable.getFirst().equals(v)) {
                    if (!globalVariables.get(globalVariable).getTypeName().equals("bool") &&
                            !globalVariables.get(globalVariable).getTypeName().equals("int") &&
                            !globalVariables.get(globalVariable).getTypeName().equals("party") &&
                            !globalVariables.get(globalVariable).getTypeName().equals("real") &&
                            !globalVariables.get(globalVariable).getTypeName().equals("str") &&
                            !globalVariables.get(globalVariable).getTypeName().equals("time") &&
                            !globalVariables.get(globalVariable).getTypeName().equals("asset")) {
                        methodSignature += "*,";
                    } else {
                        methodSignature += globalVariables.get(globalVariable).getTypeName() + ",";
                    }
                }
            }
        }

        methodSignature = methodSignature.substring(0, methodSignature.length() - 1);
        return methodSignature + "\n" + visitAgreement(context);
    }

    @Override
    public String visitAgreement(StipulaParser.AgreementContext context) {
        String body = "global:\n";
        ArrayList<String> fields = new ArrayList<>();

        for (StipulaParser.PartyContext n : context.party()) {
            parties.add(n.getText());
        }

        for (StipulaParser.VardecContext n : context.vardec()) {
            fields.add(n.getText());
        }

        // Instantiate parties
        for (String party : parties) {
            body += "GINST party " + party + "\n";
        }

        // Instantiate global variables
        for (Pair<String, Integer> globalVariable : globalVariables.keySet()) {
            if (globalVariables.get(globalVariable).getTypeName().equals("asset")) {
                body += "GINST " +
                        globalVariables.get(globalVariable).getTypeName() + " " +
                        globalVariable.getFirst() + " 2 1a3e31ad-5032-484c-9cdd-f1ed3bd760ac\n";
            } else if (globalVariables.get(globalVariable).getTypeName().equals("real")) {
                body += "GINST " + globalVariables.get(globalVariable).getTypeName() + " " + globalVariable.getFirst() + " 2\n";
            } else if (!globalVariables.get(globalVariable).getTypeName().equals("bool") &&
                    !globalVariables.get(globalVariable).getTypeName().equals("int") &&
                    !globalVariables.get(globalVariable).getTypeName().equals("party") &&
                    !globalVariables.get(globalVariable).getTypeName().equals("str") &&
                    !globalVariables.get(globalVariable).getTypeName().equals("time")) {
                body += "GINST * " + globalVariable.getFirst() + "\n";
            } else {
                body += "GINST " + globalVariables.get(globalVariable).getTypeName() + " " + globalVariable.getFirst() + "\n";
            }
        }

        // Set up parties arguments
        body += "args:" + "\n";
        for (String party : parties) {
            body += "PUSH party :" + party + "\n";
            body += "GSTORE " + party + "\n";
        }

        // Set up fields arguments
        for (String field : fields) {
            for (Pair<String, Integer> globalVariable : globalVariables.keySet()) {
                if (globalVariable.getFirst().equals(field)) {
                    body += "PUSH " + globalVariables.get(globalVariable).getTypeName() + " :" + field + "\n";
                    body += "GSTORE " + field + "\n";
                    break;
                }
            }
        }

        body += "start:\nend:\nHALT\n";
        return body;
    }

    @Override
    public ArrayList<Pair<Party, ArrayList<Field>>> visitAssign(StipulaParser.AssignContext context) {
        ArrayList<Pair<Party, ArrayList<Field>>> toRet = new ArrayList<Pair<Party, ArrayList<Field>>>();
        Pair<Party, ArrayList<Field>> pair = null;
        ArrayList<Field> fields = new ArrayList<Field>();

        for (StipulaParser.VardecContext d : context.vardec()) {
            Field tmp = new Field(d.getText());
            fields.add(tmp);
        }

        for (StipulaParser.PartyContext d : context.party()) {
            Party nd = new Party(d.getText());
            pair = new Pair<Party, ArrayList<Field>>(nd, fields);
            toRet.add(pair);
        }

        return toRet;
    }

    @Override
    public ArrayList<Asset> visitAssetdecl(StipulaParser.AssetdeclContext context) {
        ArrayList<Asset> retAssets = new ArrayList<Asset>();
        for (int i = 0; i < context.idAsset.size(); i++) {
            Asset tmpAsset = new Asset(context.idAsset.get(i).getText());
            retAssets.add(tmpAsset);
        }
        return retAssets;
    }

    @Override
    public ArrayList<Field> visitFielddecl(StipulaParser.FielddeclContext context) {
        ArrayList<Field> retFields = new ArrayList<Field>();
        for (int i = 0; i < context.idField.size(); i++) {
            Field tmpField = new Field(context.idField.get(i).getText());
            retFields.add(tmpField);
        }
        return retFields;
    }

    @Override
    public StipulaContract visitFun(StipulaParser.FunContext context) {
        String bytecode = "fn ";

        // Get the source states and the destination state for the current function
        ArrayList<String> sourceStates = new ArrayList<>();
        String destinationState = "";

        if (context.state() != null) {
            for (int i = 0; i < context.state().size(); i++) {
                String state = context.state().get(i).getText();

                if (i == context.state().size() - 1) {
                    destinationState = state;
                } else {
                    sourceStates.add(state);
                }
            }
            bytecode += sourceStates.get(0) + " "; // FIXME
        }

        ArrayList<String> parties = new ArrayList<>();
        for (StipulaParser.PartyContext party : context.party()) {
            parties.add(party.getText());
        }
        bytecode += parties.get(0) + " "; // FIXME

        String functionName = context.funId.getText();
        bytecode += functionName + " " + destinationState + " ";

        ArrayList<String> currentFunctionTypes = new ArrayList<>();
        for (String functionType : functionTypes.get(functionName)) {
            if (!functionType.equals("bool") && !functionType.equals("int") &&
                    !functionType.equals("party") && !functionType.equals("real") &&
                    !functionType.equals("str") && !functionType.equals("time") &&
                    !functionType.equals("asset")) {
                currentFunctionTypes.add("*");
            } else {
                currentFunctionTypes.add(functionType);
            }
        }

        // Add transition for state machine
        transitions.add(
                new Triple<>(
                        new DfaState(sourceStates.get(0)),
                        new DfaState(destinationState),
                        new ContractCallByParty(functionName, parties.get(0), currentFunctionTypes)
                )
        );

        for (String type : currentFunctionTypes) {
            bytecode += type + ",";
        }
        bytecode = bytecode.substring(0, bytecode.length() - 1) + "\n";

        ArrayList<String> arguments = new ArrayList<>();

        if (context.vardec() != null) {
            for (StipulaParser.VardecContext n : context.vardec()) {
                arguments.add(n.getText());
            }
        }

        if (context.assetdec() != null) {
            for (StipulaParser.AssetdecContext n : context.assetdec()) {
                arguments.add(n.getText());
            }
        }

        if (currentFunctionTypes.size() > 0) {
            bytecode += "args:\n";

            for (int i = 0; i < currentFunctionTypes.size(); i++) {
                bytecode += "PUSH " + currentFunctionTypes.get(i) + " :" + arguments.get(i) + "\n";

                if (currentFunctionTypes.get(i).equals("asset")) {
                    bytecode += "AINST " + currentFunctionTypes.get(i) + " " + arguments.get(i) + " 2 1a3e31ad-5032-484c-9cdd-f1ed3bd760ac\n";
                } else if (currentFunctionTypes.get(i).equals("real")) {
                    bytecode += "AINST " + currentFunctionTypes.get(i) + " " + arguments.get(i) + " 2\n";
                } else {
                    bytecode += "AINST " + currentFunctionTypes.get(i) + " " + arguments.get(i) + "\n";
                }

                bytecode += "ASTORE " + arguments.get(i) + "\n";
            }
        }

        bytecode += "start:\n";

        if (context.prec() != null) {
            Expression conds = visitPrec(context.prec());

            Entity left = conds.getLeft();
            Entity right = conds.getRight();

            if (left != null && right != null) {
                boolean found = false;

                for (Pair<String, Integer> globalVariable : globalVariables.keySet()) {
                    if (globalVariable.getFirst().equals(left.name)) {
                        found = true;
                        bytecode += "GLOAD " + left.name + "\n";
                    }
                }

                if (!found) {
                    bytecode += "ALOAD " + left.name + "\n";
                }

                found = false;
                for (Pair<String, Integer> globalVariable : globalVariables.keySet()) {
                    if (globalVariable.getFirst().equals(right.name)) {
                        found = true;
                        bytecode += "GLOAD " + right.name + "\n";
                    }
                }

                if (!found) {
                    bytecode += "ALOAD " + right.name + "\n";
                }

                bytecode += getBytecodeOperand(conds.getOperator()) + "\nJMPIF if_branch\nRAISE AMOUNT_NOT_EQUAL\nJMP end\nif_branch:\n";
            }
        }

        for (StipulaParser.StatContext sc : context.stat()) {
            ArrayList<Pair<Expression, ArrayList<Statement>>> ret = visitStat(sc);

            if (ret != null) {
                for (Pair<Expression, ArrayList<Statement>> pair : ret) {
                    if (pair.getFirst() == null) {
                        for (Statement statement : pair.getSecond()) {
                            if (!statement.getOperator().equals("FIELDUP")) {
                                String left = statement.getLeftExpression().getId();
                                String right = statement.getRightExpression().getId();

                                String rightTermType = "";
                                boolean found = false;
                                boolean isLeftVariableGlobal = false;
                                boolean isRightVariableGlobal = false;

                                if (statement.isFractExpressionNull()) {
                                    for (int i = 0; i < arguments.size(); i++) {
                                        if (arguments.get(i).equals(left)) {
                                            found = true;
                                            rightTermType = currentFunctionTypes.get(i);
                                            break;
                                        }
                                    }

                                    if (!found) {
                                        for (Pair<String, Integer> globalVariable : globalVariables.keySet()) {
                                            if (globalVariable.getFirst().equals(left)) {
                                                found = true;
                                                isLeftVariableGlobal = true;
                                                rightTermType = globalVariables.get(globalVariable).getTypeName();
                                                break;
                                            }
                                        }
                                    }

                                    if (!found) {
                                        // TODO
                                        // throw new Exception("Impossible to find the type for '" + right + "' variable");
                                    }
                                } else {
                                    // TODO
                                }

                                found = false;
                                for (int i = 0; i < arguments.size(); i++) {
                                    if (arguments.get(i).equals(right)) {
                                        found = true;
                                        rightTermType = currentFunctionTypes.get(i);
                                        break;
                                    }
                                }

                                if (!found) {
                                    for (Pair<String, Integer> globalVariable : globalVariables.keySet()) {
                                        if (globalVariable.getFirst().equals(right)) {
                                            found = true;
                                            isRightVariableGlobal = true;
                                            rightTermType = globalVariables.get(globalVariable).getTypeName();
                                            break;
                                        }
                                    }
                                }

                                if (!found) {
                                    for (String party : this.parties) {
                                        if (party.equals(right)) {
                                            found = true;
                                            isRightVariableGlobal = true;
                                            rightTermType = "party";
                                            break;
                                        }
                                    }
                                }

                                if (!found) {
                                    // TODO
                                    // throw new Exception("Impossible to find the type for '" + right + "' variable");
                                }

                                // Deposit
                                if (rightTermType.equals("asset")) {
                                    if (statement.isFractExpressionNull()) {
                                        if (isLeftVariableGlobal) {
                                            bytecode += "GLOAD ";
                                        } else {
                                            bytecode += "ALOAD ";
                                        }
                                        bytecode += left + "\n";
                                    } else {
                                        // TODO
                                    }

                                    if (isRightVariableGlobal) {
                                        bytecode += "GLOAD ";
                                    } else {
                                        bytecode += "ALOAD ";
                                    }
                                    bytecode += right + "\n";
                                    bytecode += "DEPOSIT " + right + "\n";
                                } else {
                                    // Withdraw

                                    // i.e. wallet -o Borrower
                                    if (statement.isFractExpressionNull()) {
                                        bytecode += "PUSH real 100 2\n";

                                        if (isLeftVariableGlobal) {
                                            bytecode += "GLOAD ";
                                        } else {
                                            bytecode += "ALOAD ";
                                        }
                                        bytecode += left + "\n";

                                        if (isRightVariableGlobal) {
                                            bytecode += "GLOAD ";
                                        } else {
                                            bytecode += "ALOAD ";
                                        }

                                        bytecode += right + "\n";
                                        bytecode += "WITHDRAW " + left + "\n";
                                    } else {
                                        // i.e. (y*wallet) -o wallet, Lender

                                    }
                                }

                            } else {
                                // FIELDUP
                                String left = statement.getLeftExpression().getId();
                                String right = statement.getRightExpression().getId();

                                String rightTermType = "";
                                boolean found = false;
                                boolean isLeftVariableGlobal = false;
                                boolean isRightVariableGlobal = false;

                                if (statement.isFractExpressionNull()) {
                                    for (int i = 0; i < arguments.size(); i++) {
                                        if (arguments.get(i).equals(left)) {
                                            found = true;
                                            rightTermType = currentFunctionTypes.get(i);
                                            break;
                                        }
                                    }

                                    if (!found) {
                                        for (Pair<String, Integer> globalVariable : globalVariables.keySet()) {
                                            if (globalVariable.getFirst().equals(left)) {
                                                found = true;
                                                isLeftVariableGlobal = true;
                                                rightTermType = globalVariables.get(globalVariable).getTypeName();
                                                break;
                                            }
                                        }
                                    }

                                    if (!found) {
                                        // TODO
                                        // throw new Exception("Impossible to find the type for '" + right + "' variable");
                                    }
                                } else {
                                    // TODO
                                }

                                found = false;
                                for (int i = 0; i < arguments.size(); i++) {
                                    if (arguments.get(i).equals(right)) {
                                        found = true;
                                        rightTermType = currentFunctionTypes.get(i);
                                        break;
                                    }
                                }

                                if (!found) {
                                    for (Pair<String, Integer> globalVariable : globalVariables.keySet()) {
                                        if (globalVariable.getFirst().equals(right)) {
                                            found = true;
                                            isRightVariableGlobal = true;
                                            rightTermType = globalVariables.get(globalVariable).getTypeName();
                                            break;
                                        }
                                    }
                                }

                                if (!found) {
                                    for (String party : this.parties) {
                                        if (party.equals(right)) {
                                            found = true;
                                            isRightVariableGlobal = true;
                                            rightTermType = "party";
                                            break;
                                        }
                                    }
                                }

                                if (!found) {
                                    // TODO
                                    // throw new Exception("Impossible to find the type for '" + right + "' variable");
                                }

                                if (!rightTermType.equals("party")) {
                                    if (statement.isFractExpressionNull()) {
                                        if (isLeftVariableGlobal) {
                                            bytecode += "GLOAD ";
                                        } else {
                                            bytecode += "ALOAD ";
                                        }
                                        bytecode += left + "\n";
                                    } else {
                                        // TODO
                                    }

                                    if (isRightVariableGlobal) {
                                        bytecode += "GSTORE ";
                                    } else {
                                        bytecode += "ASTORE ";
                                    }
                                    bytecode += right + "\n";
                                }
                            }
                        }
                    } else {
                        // newContract.addIfThenElse(ret);
                        // System.out.println("visitFun: ret => " + ret);
                    }
                }
            }
        }

        if (context.events() != null) {
            int k = 0;
            for (StipulaParser.EventsContext evn : context.events()) {
                Event event = visitEvents(evn);

                if (event != null) {
                    String left = null;
                    String right = null;
                    String operator;
                    Expression leftComplexExpression;
                    Expression rightComplexExpression;

                    if (event.getExpression().getLeft() != null) {
                        left = event.getExpression().getLeft().getId();
                    }

                    if (event.getExpression().getRight() != null) {
                        right = event.getExpression().getRight().getId();
                    }

                    if (left == null && right == null) {
                        leftComplexExpression = event.getExpression().getLeftComplexExpression();
                        rightComplexExpression = event.getExpression().getRightComplexExpression();
                        left = leftComplexExpression.getLeft().getId();
                        right = rightComplexExpression.getLeft().getId();
                    }

                    operator = getBytecodeOperand(event.getExpression().getOperator());

                    boolean found = false;
                    boolean isLeftVariableGlobal = false;
                    boolean isRightVariableGlobal = false;

                    for (int i = 0; i < arguments.size(); i++) {
                        if (arguments.get(i).equals(left)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        for (Pair<String, Integer> globalVariable : globalVariables.keySet()) {
                            if (globalVariable.getFirst().equals(left)) {
                                found = true;
                                isLeftVariableGlobal = true;
                                break;
                            }
                        }
                    }

                    if (!found) {
                        // TODO
                        // throw new Exception("Impossible to find the type for '" + right + "' variable");
                    }

                    found = false;
                    for (int i = 0; i < arguments.size(); i++) {
                        if (arguments.get(i).equals(right)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        for (Pair<String, Integer> globalVariable : globalVariables.keySet()) {
                            if (globalVariable.getFirst().equals(right)) {
                                found = true;
                                isRightVariableGlobal = true;
                                break;
                            }
                        }
                    }

                    if (right.equals("now")) {
                        bytecode += "PUSH time " + right + "\n";
                    } else {
                        if (isRightVariableGlobal) {
                            bytecode += "GLOAD ";
                        } else {
                            bytecode += "ALOAD ";
                        }
                        bytecode += right + "\n";
                    }

                    if (left.equals("now")) {
                        bytecode += "PUSH time " + left + "\n";
                    } else {
                        if (isLeftVariableGlobal) {
                            bytecode += "GLOAD ";
                        } else {
                            bytecode += "ALOAD ";
                        }
                        bytecode += left + "\n";
                    }

                    bytecode += operator + "\n";
                    String obligationFunctionName = "obligation_" + (k + 1);
                    bytecode += "TRIGGER " + obligationFunctionName + "\n";

                    String sourceState = event.getInitState();
                    String destState = event.getEndState();

                    // Add transition for state machine
                    transitions.add(
                            new Triple<>(
                                    new DfaState(sourceState),
                                    new DfaState(destState),
                                    new ContractCallByEvent(obligationFunctionName)
                            )
                    );

                    String obligationFunction = "obligation " + sourceState + " " + obligationFunctionName + " " + destState + "\nstart:\n";

                    for (Pair<Expression, ArrayList<Statement>> pair : event.getStatements()) {
                        if (pair.getFirst() == null) {
                            for (Statement statement : pair.getSecond()) {
                                // newContract.addStatement(stm);

                                if (!statement.getOperator().equals("FIELDUP")) {
                                    left = statement.getLeftExpression().getId();
                                    right = statement.getRightExpression().getId();

                                    String rightTermType = "";
                                    found = false;
                                    isLeftVariableGlobal = false;
                                    isRightVariableGlobal = false;

                                    if (statement.isFractExpressionNull()) {
                                        for (int i = 0; i < arguments.size(); i++) {
                                            if (arguments.get(i).equals(left)) {
                                                found = true;
                                                rightTermType = currentFunctionTypes.get(i);
                                                break;
                                            }
                                        }

                                        if (!found) {
                                            for (Pair<String, Integer> globalVariable : globalVariables.keySet()) {
                                                if (globalVariable.getFirst().equals(left)) {
                                                    found = true;
                                                    isLeftVariableGlobal = true;
                                                    rightTermType = globalVariables.get(globalVariable).getTypeName();
                                                    break;
                                                }
                                            }
                                        }

                                        if (!found) {
                                            // TODO
                                            // throw new Exception("Impossible to find the type for '" + right + "' variable");
                                        }
                                    } else {
                                        // TODO
                                    }

                                    found = false;
                                    for (int i = 0; i < arguments.size(); i++) {
                                        if (arguments.get(i).equals(right)) {
                                            found = true;
                                            rightTermType = currentFunctionTypes.get(i);
                                            break;
                                        }
                                    }

                                    if (!found) {
                                        for (Pair<String, Integer> globalVariable : globalVariables.keySet()) {
                                            if (globalVariable.getFirst().equals(right)) {
                                                found = true;
                                                isRightVariableGlobal = true;
                                                rightTermType = globalVariables.get(globalVariable).getTypeName();
                                                break;
                                            }
                                        }
                                    }

                                    if (!found) {
                                        for (String party : this.parties) {
                                            if (party.equals(right)) {
                                                found = true;
                                                isRightVariableGlobal = true;
                                                rightTermType = "party";
                                                break;
                                            }
                                        }
                                    }

                                    if (!found) {
                                        // TODO
                                        // throw new Exception("Impossible to find the type for '" + right + "' variable");
                                    }

                                    // Deposit
                                    if (rightTermType.equals("asset")) {
                                        if (statement.isFractExpressionNull()) {
                                            if (isLeftVariableGlobal) {
                                                obligationFunction += "GLOAD ";
                                            } else {
                                                obligationFunction += "ALOAD ";
                                            }
                                            obligationFunction += left + "\n";
                                        } else {
                                            // TODO
                                        }

                                        if (isRightVariableGlobal) {
                                            obligationFunction += "GLOAD ";
                                        } else {
                                            obligationFunction += "ALOAD ";
                                        }
                                        obligationFunction += right + "\n";
                                        obligationFunction += "DEPOSIT " + right + "\n";
                                    } else {
                                        // Withdraw

                                        // i.e. wallet -o Borrower
                                        if (statement.isFractExpressionNull()) {
                                            obligationFunction += "PUSH real 100 2\n";

                                            if (isLeftVariableGlobal) {
                                                obligationFunction += "GLOAD ";
                                            } else {
                                                obligationFunction += "ALOAD ";
                                            }
                                            obligationFunction += left + "\n";

                                            if (isRightVariableGlobal) {
                                                obligationFunction += "GLOAD ";
                                            } else {
                                                obligationFunction += "ALOAD ";
                                            }

                                            obligationFunction += right + "\n";
                                            obligationFunction += "WITHDRAW " + left + "\n";
                                        } else {
                                            // i.e. (y*wallet) -o wallet, Lender

                                        }
                                    }
                                }
                            }
                        } else {
                            // newContract.addIfThenElse(ret);
                            // System.out.println("visitFun: event.getStatements() => " + event.getStatements());
                        }
                    }
                    obligationFunction += "end:\nHALT\n";
                    obligationFunctions.add(obligationFunction);
                    k++;
                }
            }
        }
        finalBytecode += bytecode + "end:\nHALT\n";
        return null;
    }

    private String getBytecodeOperand(String operator) {
        switch (operator) {
            case "==":
                return "ISEQ";
            case "!=":
                return "ISEQ\nNOT";
            case ">":
                return "ISLE\nNOT";
            case ">=":
                return "ISLT\nNOT";
            case "<=":
                return "ISLE";
            case "<":
                return "ISLT";
            case "+":
                return "ADD";
            case "-":
                return "SUB";
            case "*":
                return "MUL";
            case "/":
                return "DIV";
        }
        return null;
    }

    @Override
    public Event visitEvents(StipulaParser.EventsContext context) {
        if (context.EMPTY() == null) {
            String init = context.ID(0).toString();
            String end = context.ID(context.ID().size() - 1).toString();
            ArrayList<Pair<Expression, ArrayList<Statement>>> eventStat = new ArrayList<>();

            for (StipulaParser.StatContext stm : context.stat()) {
                eventStat.addAll(visitStat(stm));
            }

            Expression expr = visitExpr(context.expr());
            Event eventToRet = new Event(init, end, eventStat, expr);
            return eventToRet;
        } else {
            return null;
        }
    }

    @Override
    public ArrayList<Pair<Expression, ArrayList<Statement>>> visitStat(StipulaParser.StatContext context) {
        ArrayList<Pair<Expression, ArrayList<Statement>>> ret = null;

        if (context.ASSETUP() != null) {
            Asset left;
            Asset right;

            if (context.COMMA() != null) {
                if (context.left.expr() != null) {
                    Expression expr = visitExpr(context.left.expr());
                    double fract = 0;
                    Entity fractExpr = null;

                    if (expr.getRight() != null) {
                        left = new Asset(expr.getRight().getId());
                        right = new Asset(context.right.getText());

                        try {
                            fract = Double.parseDouble(expr.getLeft().getId());
                        } catch (NumberFormatException e) {
                            fractExpr = new Entity(expr.getLeft().getId());
                        }
                    } else {
                        left = new Asset(expr.getRightComplexExpression().getLeft().getId());
                        right = new Asset(context.rightPlus.getText());

                        try {
                            fract = Double.parseDouble(expr.getLeftComplexExpression().getLeft().getId());
                        } catch (NumberFormatException e) {
                            fractExpr = new Entity((expr.getLeftComplexExpression().getLeft().getId()));
                        }
                    }

                    ArrayList<Statement> tmpArray = new ArrayList<Statement>();
                    if (fractExpr == null) {
                        tmpArray.add(new Statement(left, right, "ASSETUP", fract));
                    } else {
                        tmpArray.add(new Statement(left, right, "ASSETUP", fractExpr));
                    }

                    Pair<Expression, ArrayList<Statement>> tmpPair = new Pair<>(null, tmpArray);
                    ret = new ArrayList<>();
                    ret.add(tmpPair);
                } else {
                    left = new Asset(context.right.getText());
                    right = new Asset(context.rightPlus.getText());

                    ArrayList<Statement> tmpArray = new ArrayList<>();
                    tmpArray.add(new Statement(left, right, "ASSETUP"));

                    Pair<Expression, ArrayList<Statement>> tmpPair = new Pair<>(null, tmpArray);
                    ret = new ArrayList<>();
                    ret.add(tmpPair);
                }
            } else if (context.left.expr() != null) {
                Expression expr = visitExpr(context.left.expr());
                double fract = 0;
                Entity fractExpr = null;

                if (expr.getRight() != null) {
                    left = new Asset(expr.getRight().getId());
                    right = new Asset(context.right.getText());

                    try {
                        fract = Double.parseDouble(expr.getLeft().getId());
                    } catch (NumberFormatException e) {
                        fractExpr = new Entity(expr.getLeft().getId());
                    }
                } else {
                    left = new Asset(expr.getRightComplexExpression().getLeft().getId());
                    right = new Asset(context.right.getText());

                    try {
                        fract = Double.parseDouble(expr.getLeftComplexExpression().getLeft().getId());
                    } catch (NumberFormatException e) {
                        fractExpr = new Entity((expr.getLeftComplexExpression().getLeft().getId()));
                    }
                }

                ArrayList<Statement> tmpArray = new ArrayList<>();
                if (fractExpr == null) {
                    tmpArray.add(new Statement(left, right, "ASSETUP", fract));
                } else {
                    tmpArray.add(new Statement(left, right, "ASSETUP", fractExpr));
                }

                Pair<Expression, ArrayList<Statement>> tmpPair = new Pair<>(null, tmpArray);
                ret = new ArrayList<>();
                ret.add(tmpPair);
            } else {
                left = new Asset(context.left.getText());
                right = new Asset(context.right.getText());

                ArrayList<Statement> tmpArray = new ArrayList<>();
                tmpArray.add(new Statement(left, right, "ASSETUP"));

                Pair<Expression, ArrayList<Statement>> tmpPair = new Pair<>(null, tmpArray);
                ret = new ArrayList<>();
                ret.add(tmpPair);
            }
        } else if (context.FIELDUP() != null) {
            Field left = new Field(context.left.getText());
            Field right = new Field(context.right.getText());
            ArrayList<Statement> tmpArray = new ArrayList<>();
            tmpArray.add(new Statement(left, right, "FIELDUP"));
            Pair<Expression, ArrayList<Statement>> tmpPair = new Pair<>(null, tmpArray);
            ret = new ArrayList<>();
            ret.add(tmpPair);
        } else if (context.ifelse() != null) {
            ret = visitIfelse(context.ifelse());
        }

        return ret;
    }

    @Override
    public ArrayList<Pair<Expression, ArrayList<Statement>>> visitIfelse(StipulaParser.IfelseContext context) {
        Expression condIf = visitExpr(context.cond);
        ArrayList<Pair<Expression, ArrayList<Statement>>> toRet = new ArrayList<Pair<Expression, ArrayList<Statement>>>();
        ArrayList<Statement> tmpStat = new ArrayList<Statement>();
        int start = 0;
        boolean flag = false;

        for (int i = start; i < context.ifBranch.size() && !flag; i++) {
            if (context.ifBranch.get(i).getText().equals("_")) {
                flag = true;
                start = i + 1;
            } else {
                ArrayList<Pair<Expression, ArrayList<Statement>>> tmpRet = visitStat(context.ifBranch.get(i));
                for (Pair<Expression, ArrayList<Statement>> pair : tmpRet) {
                    if (pair.getFirst() == null) {
                        for (Statement stm : pair.getSecond()) {
                            tmpStat.add(stm);
                        }
                    }
                }
            }
        }

        Pair<Expression, ArrayList<Statement>> tmp = new Pair<Expression, ArrayList<Statement>>(condIf, tmpStat);
        toRet.add(tmp);

        if (context.condElseIf != null) {
            flag = false;
            tmpStat = new ArrayList<Statement>();

            for (StipulaParser.ExprContext expr : context.condElseIf) {
                for (int i = start; i < context.elseIfBranch.size() && !flag; i++) {
                    if (context.elseIfBranch.get(i).getText().equals("_")) {
                        flag = true;
                        start = i + 1;
                    } else {
                        ArrayList<Pair<Expression, ArrayList<Statement>>> tmpRet = visitStat(context.elseIfBranch.get(i));

                        for (Pair<Expression, ArrayList<Statement>> pair : tmpRet) {
                            if (pair.getFirst() == null) {
                                for (Statement stm : pair.getSecond()) {
                                    tmpStat.add(stm);
                                }
                            }
                        }
                    }
                }
                tmp = new Pair<>(visitExpr(expr), tmpStat);
                toRet.add(tmp);
            }
        }

        if (context.elseBranch != null) {
            tmpStat = new ArrayList<Statement>();

            for (StipulaParser.StatContext stm : context.elseBranch) {
                ArrayList<Pair<Expression, ArrayList<Statement>>> tmpRet = visitStat(stm);

                for (Pair<Expression, ArrayList<Statement>> pair : tmpRet) {
                    if (pair.getFirst() == null) {
                        for (Statement stm2 : pair.getSecond()) {
                            tmpStat.add(stm2);
                        }
                    }
                }
            }
            tmp = new Pair<Expression, ArrayList<Statement>>(new Expression(new Entity("_")), tmpStat);
            toRet.add(tmp);
        }
        return toRet;
    }

    @Override
    public Expression visitPrec(StipulaParser.PrecContext context) {
        return visitExpr(context.expr());
    }

    @Override
    public Expression visitExpr(StipulaParser.ExprContext context) {
        if (context.right == null) {
            if (context.left.right == null) {
                if (context.left.left.right == null) {
                    return visitValue(context.left.left.left);
                } else {
                    Entity left = visitValue(context.left.left.left).getLeft();
                    Entity right = visitValue(context.left.left.right).getLeft();
                    String operator = context.left.left.operator.getText();
                    return new Expression(left, right, operator);
                }
            } else {
                Expression leftExpression = visitFactor(context.left.left);
                Expression rightExpression = visitTerm(context.left.right);
                String operator = context.left.operator.getText();
                return new Expression(leftExpression, rightExpression, operator);
            }
        } else {
            Expression leftExpression = visitTerm(context.left);
            Expression rightExpression = visitExpr(context.right);
            String operator = context.operator.getText();
            return new Expression(leftExpression, rightExpression, operator);
        }
    }

    @Override
    public Expression visitTerm(StipulaParser.TermContext ctx) {
        if (ctx.right != null) {
            Entity left = (Entity) visit(ctx.left);
            Entity right = (Entity) visit(ctx.right);
            return new Expression(left, right, ctx.operator.getText());
        } else {
            return visitFactor(ctx.left);
        }
    }

    @Override
    public Expression visitFactor(StipulaParser.FactorContext ctx) {
        if (ctx.right != null) {
            Entity left = visitValue(ctx.left).getLeft();
            Entity right = visitValue(ctx.right).getLeft();
            Expression toRet = new Expression(left, right, ctx.operator.getText());
            return toRet;
        } else if (ctx.operator != null) {
            return new Expression(visitValue(ctx.left).getLeft(), ctx.operator.getText());
        } else {
            return new Expression(visitValue(ctx.left).getLeft());
        }
    }

    @Override
    public Expression visitValue(StipulaParser.ValueContext ctx) {
        Expression ret = null;

        if (ctx.NOW() != null) {
            ret = new Expression(new Entity(ctx.NOW().getText()), null);
        } else if (ctx.TRUE() != null) {
            ret = new Expression(new Entity(ctx.TRUE().getText()), null);
        } else if (ctx.FALSE() != null) {
            ret = new Expression(new Entity(ctx.FALSE().getText()), null);
        } else if (ctx.EMPTY() != null) {
            ret = new Expression(new Entity(""), null);
        } else if (ctx.RAWSTRING() != null) {
            ret = new Expression(new Entity(ctx.RAWSTRING().getText()), null);
        } else if (ctx.ID() != null) {
            ret = new Expression(new Entity(ctx.ID().getText()), null);
        } else if (ctx.number() != null) {
            ret = new Expression(new Entity(ctx.number().getText()), null);
        } else if (ctx.expr() != null) {
            ret = visitExpr(ctx.expr());
        }

        return ret;
    }
}
