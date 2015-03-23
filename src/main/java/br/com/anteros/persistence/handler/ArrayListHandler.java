package br.com.anteros.persistence.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArrayListHandler implements ResultSetHandler {
	
	static final RowProcessor ROW_PROCESSOR = new BasicRowProcessor();

    private final RowProcessor convert;

    public ArrayListHandler() {
        this(ROW_PROCESSOR);
    }

    public ArrayListHandler(RowProcessor convert) {
        super();
        this.convert = convert;
    }
	
	public List<Object> handle(ResultSet rs) throws SQLException {
		List<Object> rows = new ArrayList<Object>();
		while (rs.next()) {
			rows.add(this.handleRow(rs));
		}
		return rows;
	}

	protected Object[] handleRow(ResultSet rs) throws SQLException {
		return convert.toArray(rs);
	}


}
