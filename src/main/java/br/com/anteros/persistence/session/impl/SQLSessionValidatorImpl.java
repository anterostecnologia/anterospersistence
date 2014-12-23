package br.com.anteros.persistence.session.impl;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.persistence.session.SQLSessionValidatior;
import br.com.anteros.persistence.util.AnterosBeanValidationHelper;

public class SQLSessionValidatorImpl implements SQLSessionValidatior {
	
	private Logger LOG = LoggerProvider.getInstance().getLogger(SQLSessionValidatior.class);

	@Override
	public void validateBean(Object object) throws Exception {
		Validator validator = AnterosBeanValidationHelper.getBeanValidator();
		final Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object);
		if (constraintViolations.size() > 0) {
			Set<ConstraintViolation<?>> propagatedViolations = new HashSet<ConstraintViolation<?>>(
					constraintViolations.size());
			Set<String> classNames = new HashSet<String>();
			for (ConstraintViolation<?> violation : constraintViolations) {
				LOG.debug(violation);
				propagatedViolations.add(violation);
				classNames.add(violation.getLeafBean().getClass().getName());
			}
			StringBuilder builder = new StringBuilder();
			builder.append("Validation failed for classes [");
			builder.append(classNames);
			builder.append("] during saving object.");
			builder.append("\nList of constraint violations:[\n");
			for (ConstraintViolation<?> violation : constraintViolations) {
				builder.append("\t").append(violation.toString()).append("\n");
			}
			builder.append("]");

			throw new ConstraintViolationException(builder.toString(), propagatedViolations);
		}
	}

}
