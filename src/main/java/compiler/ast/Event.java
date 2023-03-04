package compiler.ast;

import lib.datastructures.Pair;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Event {
    private final int SECS = 60;
    private final int MINS = 60;
    private final String sourceState;
    private final String destinationState;
    private final ArrayList<Pair<Expression, ArrayList<Statement>>> statements;
    private final Expression expression;
    private Timer timer;
    private StipulaContract stipulaContract;

    public Event(String sourceState, String destinationState, ArrayList<Pair<Expression, ArrayList<Statement>>> statements, Expression expression) {
        this.sourceState = sourceState;
        this.destinationState = destinationState;
        this.statements = statements;
        this.expression = expression;
    }

    public void addContract(StipulaContract c) {
        stipulaContract = c;
    }

    public StipulaContract getContract() {
        return stipulaContract;
    }

    public long evaluateEvent(Program program) {
        long seconds = 0;
        DateUtils d = new DateUtils();

        if (expression.getOperator() == null) {
            Entity left = expression.getLeft();
            int indexVar = stipulaContract.findVar(left.getId(), program.getFields());
            program.getFields().get(indexVar).setType(new TimeType());
            stipulaContract.setValuesConditions(left, null);
            if (!left.getValueStr().equals("")) {
                seconds = d.calculateSeconds(left.getValueStr());
            } else {
                seconds = (int) (left.getValue() * SECS * MINS);
            }

        } else if (expression.getLeftComplexExpression() != null) {
            Entity left = expression.getLeftComplexExpression().getLeft();
            Entity right = null;

            if (expression.getRightComplexExpression() != null) {
                right = expression.getRightComplexExpression().getLeft();
            } else {
                right = expression.getLeftComplexExpression().getRight();
            }


            String op = expression.getOperator();
            int indexVarLeft;
            int indexVarRight;
            if (left.getId().equals("now")) {
                left.setValue(0);
                indexVarRight = stipulaContract.findVar(right.getId(), program.getFields());
                right.setValue(program.getFields().get(indexVarRight).getValue());

                program.getFields().get(indexVarRight).setType(new TimeType());


                stipulaContract.setValuesConditions(null, right);
            } else if (right.getId().equals("now")) {
                right.setValue(0);
                indexVarLeft = stipulaContract.findVar(left.getId(), program.getFields());
                program.getFields().get(indexVarLeft).setType(new TimeType());
                left.setValue(program.getFields().get(indexVarLeft).getValue());
                stipulaContract.setValuesConditions(left, null);
            } else {
                indexVarLeft = stipulaContract.findVar(left.getId(), program.getFields());
                program.getFields().get(indexVarLeft).setType(new TimeType());
                left.setValue(program.getFields().get(indexVarLeft).getValue());

                indexVarRight = stipulaContract.findVar(right.getId(), program.getFields());
                program.getFields().get(indexVarRight).setType(new TimeType());
                right.setValue(program.getFields().get(indexVarRight).getValue());

                stipulaContract.setValuesConditions(left, right);
            }
            if (op.equals("+")) {

                if (!left.getValueStr().equals("")) {
                    seconds = (int) (d.calculateSeconds(left.getValueStr()) + right.getValue() * SECS * MINS);
                } else if (!right.getValueStr().equals("")) {
                    seconds = (int) (left.getValue() * SECS * MINS + d.calculateSeconds(right.getValueStr()));
                } else {

                    seconds = (int) (left.getValue() + right.getValue() * SECS * MINS);
                }
            }

        } else if (expression.getTextExpression().equals("now")) {
            seconds = 0;
        }

        return seconds;
    }

    public void setTimer(int seconds) {
        Object lock = new Object();

        timer = new Timer();
        timer.schedule(new DelayEvent(lock), seconds * 1000);
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException ex) {
            }
        }
    }

    class DelayEvent extends TimerTask {
        private Object lock;

        public DelayEvent(Object lock) {
            this.lock = lock;
        }

        public void run() {
            synchronized (lock) {
                lock.notifyAll();
            }
            timer.cancel();
        }
    }

    public String getInitState() {
        return sourceState;
    }

    public String getEndState() {
        return destinationState;
    }

    public ArrayList<Pair<Expression, ArrayList<Statement>>> getStatements() {
        return statements;
    }

    public Expression getExpression() {
        return expression;
    }

    public String printEvent() {
        String ret = expression.getTextExpression() + " >> @" + sourceState + "{\n\t";
        for (Pair<Expression, ArrayList<Statement>> pair : statements) {
            if (pair.getFirst() != null) {
                ret = ret + pair.getFirst().getTextExpression();
            }
            for (Statement stm : pair.getSecond()) {
                ret = ret + stm.getTextStatement();
                ret = ret + "\n\t";
            }
        }
        ret = ret + " } ==> @" + destinationState;
        return ret;
    }

    @Override
    public String toString() {
        return "Event{" +
                "SECS=" + SECS +
                ", MINS=" + MINS +
                ", init='" + sourceState + '\'' +
                ", end='" + destinationState + '\'' +
                ", statements=" + statements +
                ", expr=" + expression +
                ", timer=" + timer +
                ", contract=" + stipulaContract +
                '}';
    }
}
