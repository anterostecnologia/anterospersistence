package br.com.anteros.persistence.sql.lob;

import java.sql.NClob;

public class AnterosNClob extends AnterosClob implements NClob {

    protected AnterosNClob() {
        super();
    }

    public AnterosNClob(String data) throws java.sql.SQLException {
        super(data);
    }
}