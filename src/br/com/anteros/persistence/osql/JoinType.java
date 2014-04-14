package br.com.anteros.persistence.osql;

public enum JoinType {
    DEFAULT(false, false),
    INNERJOIN(true, false),
    JOIN(true, false),
    LEFTJOIN(false, true),
    RIGHTJOIN(false, true),
    FULLJOIN(false, true);

    private final boolean inner, outer;
    
    JoinType(boolean inner, boolean outer) {
        this.inner = inner;
        this.outer = outer;
    }
    
    public boolean isInner() {
    	
        return inner;
    }
    
    public boolean isOuter() {
        return outer;
    }
    
}
