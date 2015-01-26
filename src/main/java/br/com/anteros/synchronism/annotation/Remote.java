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
package br.com.anteros.synchronism.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.anteros.persistence.metadata.descriptor.type.ConnectivityType;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface Remote {

	String displayLabel();

	String mobileActionExport() default "";

	String mobileActionImport() default "";

	RemoteParam[] importParams() default {};

	RemoteParam[] exportParams() default {};

	int exportOrderToSendData() default 0;

	String[] exportColumns() default {};

	ConnectivityType importConnectivityType() default ConnectivityType.ALL_CONNECTION;

	ConnectivityType exportConnectivityType() default ConnectivityType.ALL_CONNECTION;

}
