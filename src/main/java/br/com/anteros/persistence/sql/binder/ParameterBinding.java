package br.com.anteros.persistence.sql.binder;

import java.sql.PreparedStatement;

public interface ParameterBinding {

	void bindValue(PreparedStatement statement, int parameterIndex) throws Exception;
}
