package br.com.anteros.persistence.osql;

@SuppressWarnings("serial")
public class ConditionException extends RuntimeException{

    public ConditionException(String msg) {
        super(msg);
    }

    public ConditionException(String msg, Throwable t) {
        super(msg, t);
    }

    public ConditionException(Throwable t) {
        super(t);
    }

}
