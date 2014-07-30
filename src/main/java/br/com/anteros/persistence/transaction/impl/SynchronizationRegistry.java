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
