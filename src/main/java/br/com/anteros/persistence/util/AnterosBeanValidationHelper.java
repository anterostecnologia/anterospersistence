/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
