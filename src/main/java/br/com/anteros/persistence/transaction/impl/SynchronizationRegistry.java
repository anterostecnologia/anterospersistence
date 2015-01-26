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
package br.com.anteros.persistence.transaction.impl;

import java.util.LinkedHashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.anteros.persistence.transaction.AnterosSynchronization;

public class SynchronizationRegistry {
	private static final Logger log = LoggerFactory.getLogger(SynchronizationRegistry.class);

	private LinkedHashSet<AnterosSynchronization> synchronizations;

	public void registerSynchronization(AnterosSynchronization synchronization) {
		if (synchronization == null) {
			throw new NullSynchronizationException();
		}

		if (synchronizations == null) {
			synchronizations = new LinkedHashSet<AnterosSynchronization>();
		}

		boolean added = synchronizations.add(synchronization);
		if (!added) {
			log.info("Synchronization [{}] was already registered", synchronization);
		}
	}

	public void notifySynchronizationsBeforeTransactionCompletion() {
		if (synchronizations != null) {
			for (AnterosSynchronization synchronization : synchronizations) {
				try {
					synchronization.beforeCompletion();
				} catch (Throwable t) {
					log.error("exception calling user Synchronization [{}]", synchronization, t);
				}
			}
		}
	}

	public void notifySynchronizationsAfterTransactionCompletion(int status) {
		if (synchronizations != null) {
			for (AnterosSynchronization synchronization : synchronizations) {
				try {
					synchronization.afterCompletion(status);
				} catch (Throwable t) {
					log.error("exception calling user Synchronization [{}]", synchronization, t);
				}
			}
		}
	}
}
