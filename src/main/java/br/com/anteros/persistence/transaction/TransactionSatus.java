package br.com.anteros.persistence.transaction;

public enum TransactionSatus {

	NOT_ACTIVE,

	ACTIVE,

	COMMITTED,

	ROLLED_BACK,

	FAILED_COMMIT;
}
