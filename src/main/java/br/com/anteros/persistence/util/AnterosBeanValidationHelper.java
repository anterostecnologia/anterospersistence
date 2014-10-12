package br.com.anteros.persistence.util;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class AnterosBeanValidationHelper {

	public static final String VALIDATION_PROVIDER_CLASSNAME = "br.com.anteros.bean.validation.AnterosValidationProvider";
	
	private static Validator validator;

	private AnterosBeanValidationHelper() {
	}
	
	public static boolean isBeanValidationPresent(){
		try {
			Class.forName(VALIDATION_PROVIDER_CLASSNAME);
			return true;
		} catch (ClassNotFoundException e) {
		}
		return false;
	}

	public static Validator getBeanValidator(){
		if (validator == null){
			ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
			validator = validatorFactory.getValidator();
		}
		return validator;
	}

}
