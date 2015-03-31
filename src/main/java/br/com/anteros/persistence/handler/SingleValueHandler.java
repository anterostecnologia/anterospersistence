package br.com.anteros.persistence.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.anteros.core.utils.ObjectUtils;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.sql.format.SqlFormatRule;
import br.com.anteros.persistence.sql.parser.INode;
import br.com.anteros.persistence.sql.parser.ParserUtil;
import br.com.anteros.persistence.sql.parser.SqlParser;
import br.com.anteros.persistence.sql.parser.node.ColumnNode;
import br.com.anteros.persistence.sql.parser.node.RootNode;
import br.com.anteros.persistence.sql.parser.node.SelectStatementNode;

@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
public class SingleValueHandler implements ScrollableResultSetHandler {

	private Class type;
	private String aliasColumnName;
	private DescriptionField descriptionField;
	private int columnIndex;
	private String sql;

	public SingleValueHandler(String sql, Class type, DescriptionField descriptionField, String aliasColumnName, int columnIndex) {
		this.type = type;
		this.aliasColumnName = aliasColumnName;
		this.descriptionField = descriptionField;
		this.columnIndex = columnIndex;
		this.sql = sql;
	}

	@Override
	public List<?> handle(ResultSet rs) throws Exception {
		List result = null;
		if (rs.next()) {
			result = new ArrayList();
			do {
				Object value = readRow(rs);
				result.add(value);
			} while (rs.next());
		}
		return result;
	}

	protected Object readRow(ResultSet rs) throws SQLException, Exception {
		Object value = null;
		if (columnIndex != -1)
			value = rs.getObject(columnIndex);
		else
			value = rs.getObject(getColumnIndex(aliasColumnName));
		if ((descriptionField != null) && (descriptionField.hasConverts()))
			value = descriptionField.getSimpleColumn().convertToEntityAttribute(value);
		else
			value = ObjectUtils.convert(value, type);
		return value;
	}

	@Override
	public Object[] readCurrentRow(ResultSet resultSet) throws Exception {
		Object value = readRow(resultSet);
		return new Object[] { value };
	}
	
	protected int getColumnIndex(String columnName) {
		SqlParser parser = new SqlParser(sql, new SqlFormatRule());
		INode node = new RootNode();
		parser.parse(node);

		INode[] children = (INode[]) ParserUtil.findChildren(getFirstSelectStatement(node), ColumnNode.class.getSimpleName());
		int i = 1;
		/*
		 * Localiza primeiro pelo alias da coluna caso o usuário tenha informado. O alias definido pelo usuário não será alterado durante a análise do sql.
		 */
		for (INode nd : children) {
			if (nd instanceof ColumnNode) {
				ColumnNode columnNode = (ColumnNode) nd;
				if (columnNode.hasAlias()) {
					if (columnNode.getAliasName().equalsIgnoreCase(columnName)) {
						return i;
					}
				}
				i++;
			}
		}
		
		/*
		 * Caso não encontre pelo alias da coluna procura pelo nome da coluna. 
		 */
		i = 1;
		for (INode nd : children) {
			if (nd instanceof ColumnNode) {
				ColumnNode columnNode = (ColumnNode) nd;
				if (columnNode.getColumnName().equalsIgnoreCase(columnName)) {
					return i;
				}
				i++;
			}
		}
		return -1;
	}

	/**
	 * Retorna o primeiro nó do tipo SelectStatement da árvore do SQL
	 * 
	 * @param node
	 *            Árvore do SQL
	 * @return Nó que representa o SelectStatement
	 */
	private SelectStatementNode getFirstSelectStatement(INode node) {
		return (SelectStatementNode) ParserUtil.findFirstChild(node, "SelectStatementNode");
	}
}
