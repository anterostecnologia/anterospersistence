package br.com.anteros.persistence.session.lock;

import java.util.Set;

import br.com.anteros.core.utils.CompactHashSet;

public class LockOptions {

	public static final int NO_WAIT = 0;

	public static final int WAIT_FOREVER = -1;

	public static final LockOptions READ = new LockOptions(LockMode.READ);
	public static final LockOptions WRITE = new LockOptions(LockMode.WRITE);
	public static final LockOptions OPTIMISTIC = new LockOptions(LockMode.OPTIMISTIC);
	public static final LockOptions OPTIMISTIC_FORCE_INCREMENT = new LockOptions(LockMode.OPTIMISTIC_FORCE_INCREMENT);
	public static final LockOptions PESSIMISTIC_READ = new LockOptions(LockMode.PESSIMISTIC_READ);
	public static final LockOptions PESSIMISTIC_WRITE = new LockOptions(LockMode.PESSIMISTIC_WRITE);
	public static final LockOptions PESSIMISTIC_FORCE_INCREMENT = new LockOptions(LockMode.PESSIMISTIC_FORCE_INCREMENT);
	public static final LockOptions NONE = new LockOptions(LockMode.NONE);

	private LockMode lockMode;

	private int timeout = WAIT_FOREVER;

	private Set<String> aliasesToLock;

	private LockScope lockScope = LockScope.NORMAL;

	public LockOptions() {
	}

	public LockOptions(LockMode lockMode) {
		this.lockMode = lockMode;
	}

	public LockMode getLockMode() {
		return lockMode;
	}

	public LockOptions setLockMode(LockMode lockMode) {
		this.lockMode = lockMode;
		return this;
	}

	public LockOptions addAliasToLock(String alias) {
		if (aliasesToLock == null) {
			aliasesToLock = new CompactHashSet<String>();
		}
		aliasesToLock.add(alias);
		return this;
	}

	public int getTimeOut() {
		return timeout;
	}

	public LockOptions setTimeOut(int timeout) {
		this.timeout = timeout;
		return this;
	}

	public LockOptions makeCopy() {
		final LockOptions copy = new LockOptions();
		copy(this, copy);
		return copy;
	}

	public static LockOptions copy(LockOptions source, LockOptions destination) {
		destination.setLockMode(source.getLockMode());
		destination.setTimeOut(source.getTimeOut());
		if (source.aliasesToLock != null) {
			destination.aliasesToLock = new CompactHashSet<String>(source.aliasesToLock);
		}
		return destination;
	}

	public LockScope getLockScope() {
		return lockScope;
	}

	public LockOptions setLockScope(LockScope lockScope) {
		this.lockScope = lockScope;
		return this;
	}

	public boolean contains(LockMode... locks) {
		for (LockMode lock : locks) {
			if (lock == lockMode)
				return true;
		}
		return false;
	}

}

// PESSIMISTIC_READ lock in share mode
// PESSIMISTIC_WRITE for update
// PESSIMISTIC_FORCE_INCREMENT for update e usa @Version e incrementa mesmo não havendo alteração
// OPTIMISTIC e READ Usa @Version só incrementa se houve alteração
// OPTIMISTIC_FORCE_INCREMENT e WRITE Usa @Version incrementa mesmo sem alteração

// org.hibernate.HibernateException: [OPTIMISTIC_FORCE_INCREMENT] not supported for non-versioned entities
// [com.journaldev.servlet.hibernate.model.Cliente]
// org.hibernate.OptimisticLockException: [OPTIMISTIC] not supported for non-versioned entities
// [com.journaldev.servlet.hibernate.model.Cliente]
// org.hibernate.HibernateException: [PESSIMISTIC_FORCE_INCREMENT] not supported for non-versioned entities
// [com.journaldev.servlet.hibernate.model.Cliente]