package br.com.anteros.persistence.service;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.dao.SQLDao;

/**
 * 
 * Classe que representa um serviço para o Anteros com o papel de armazenar a
 * regra de negócio e fazer o acesso ao banco de dados através do SQLDao
 * 
 * @author Douglas Junior <nassifrroma@gmail.com>
 *
 */
public abstract class AbstractSQLService<T> {

	protected SQLDao<T> dao;

	public AbstractSQLService(SQLSessionFactory sqlSessionFactory) {
		try {
			dao = new SQLDao<T>(sqlSessionFactory.getSession(),
					// descobre automaticamente qual a classe do tipo T
					(Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public T selectOne(String sql, Object[] parameter) throws Exception {
		return (T) dao.selectOne(sql, parameter);
	}

	public List<T> selectList(String sql) throws Exception {
		return dao.selectList(sql);
	}

	public List<T> selectList(String sql, Object[] parameter) throws Exception {
		return dao.selectList(sql, parameter);
	}

	public T save(T object) throws Exception {
		return (T) dao.save(object);
	}

	public void remove(T object) throws Exception {
		dao.remove(object);
	}

}
