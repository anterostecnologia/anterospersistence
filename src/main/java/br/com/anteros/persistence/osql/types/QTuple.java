/*
 * Copyright 2011, Mysema Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.anteros.persistence.osql.types;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import br.com.anteros.persistence.osql.Tuple;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * QTuple represents a projection of type Tuple
 *
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * {
 * 	&#064;code
 * 	List&lt;Tuple&gt; result = query.from(employee).list(new QTuple(employee.firstName, employee.lastName));
 * 	for (Tuple row : result) {
 * 		System.out.println(&quot;firstName &quot; + row.get(employee.firstName));
 * 		System.out.println(&quot;lastName &quot; + row.get(employee.lastName));
 * 	}
 * }
 * </pre>
 *
 * <p>
 * Since Tuple projection is the default for multi column projections, the above
 * is equivalent to this code
 * </p>
 *
 * <pre>
 * {
 * 	&#064;code
 * 	List&lt;Tuple&gt; result = query.from(employee).list(employee.firstName, employee.lastName);
 * 	for (Tuple row : result) {
 * 		System.out.println(&quot;firstName &quot; + row.get(employee.firstName));
 * 		System.out.println(&quot;lastName &quot; + row.get(employee.lastName));
 * 	}
 * }
 * </pre>
 *
 * @author tiwe
 *
 */
public class QTuple extends ExpressionBase<Tuple> implements FactoryExpression<Tuple> {

	private static ImmutableMap<Expression<?>, Integer> createBindings(List<Expression<?>> exprs) {
		Map<Expression<?>, Integer> map = Maps.newHashMap();
		for (int i = 0; i < exprs.size(); i++) {
			Expression<?> e = exprs.get(i);
			if (e instanceof Operation && ((Operation<?>) e).getOperator() == Ops.ALIAS) {
				map.put(((Operation<?>) e).getArg(1), i);
			}
			map.put(e, i);
		}
		return ImmutableMap.copyOf(map);
	}

	private final class TupleImpl implements Tuple, Serializable {

		private static final long serialVersionUID = 6635924689293325950L;

		private final Object[] a;

		private TupleImpl(Object[] a) {
			this.a = a;
		}

		
		public <T> T get(int index, Class<T> type) {
			return (T) a[index];
		}

		
		public <T> T get(Expression<T> expr) {
			Integer idx = QTuple.this.bindings.get(expr);
			if (idx != null) {
				return (T) a[idx.intValue()];
			} else {
				return null;
			}
		}

		
		public int size() {
			return a.length;
		}

		
		public Object[] toArray() {
			return a;
		}

		
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			} else if (obj instanceof Tuple) {
				return Arrays.equals(a, ((Tuple) obj).toArray());
			} else {
				return false;
			}
		}

		
		public int hashCode() {
			return Arrays.hashCode(a);
		}

		
		public String toString() {
			return Arrays.toString(a);
		}
	}

	private static final long serialVersionUID = -2640616030595420465L;

	private final ImmutableList<Expression<?>> args;

	private final ImmutableMap<Expression<?>, Integer> bindings;

	/**
	 * Create a new QTuple instance
	 *
	 * @param args
	 */
	public QTuple(Expression<?>... args) {
		super(Tuple.class);
		this.args = ImmutableList.copyOf(args);
		this.bindings = createBindings(this.args);
	}

	/**
	 * Create a new QTuple instance
	 *
	 * @param args
	 */
	public QTuple(ImmutableList<Expression<?>> args) {
		super(Tuple.class);
		this.args = args;
		this.bindings = createBindings(this.args);
	}

	/**
	 * Create a new QTuple instance
	 *
	 * @param args
	 */
	public QTuple(Expression<?>[]... args) {
		super(Tuple.class);
		ImmutableList.Builder<Expression<?>> builder = ImmutableList.builder();
		for (Expression<?>[] exprs : args) {
			builder.add(exprs);
		}
		this.args = builder.build();
		this.bindings = createBindings(this.args);
	}

	
	public Tuple newInstance(Object... a) {
		return new TupleImpl(a);
	}

	
	public <R, C> R accept(Visitor<R, C> v, C context) {
		return v.visit(this, context);
	}

	
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof FactoryExpression) {
			FactoryExpression<?> c = (FactoryExpression<?>) obj;
			return args.equals(c.getArgs()) && getType().equals(c.getType());
		} else {
			return false;
		}
	}

	
	public List<Expression<?>> getArgs() {
		return args;
	}

}
