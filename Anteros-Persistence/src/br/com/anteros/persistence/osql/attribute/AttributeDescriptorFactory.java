package br.com.anteros.persistence.osql.attribute;

import br.com.anteros.persistence.osql.condition.Condition;

public final class AttributeDescriptorFactory {

	public static AttributeDescriptor<Integer> createArrayAccessor(Attribute<?> parent, Condition<Integer> index) {
		return new AttributeDescriptor<Integer>(parent, index, AttributeRelationType.ARRAYVALUE);
	}

	public static AttributeDescriptor<Integer> createArrayAccessor(Attribute<?> parent, int index) {
		return new AttributeDescriptor<Integer>(parent, index, AttributeRelationType.ARRAYVALUE_CONSTANT);
	}

	public static AttributeDescriptor<?> createCollectionAnyAccessor(Attribute<?> parent) {
		return new AttributeDescriptor<String>(parent, "", AttributeRelationType.COLLECTION_ANY);
	}

	public static <T> AttributeDescriptor<T> createDelegateAccessor(Attribute<T> delegate) {
		return new AttributeDescriptor<T>(delegate, delegate, AttributeRelationType.DELEGATE);
	}

	public static AttributeDescriptor<Integer> createListAccessor(Attribute<?> parent, Condition<Integer> index) {
		return new AttributeDescriptor<Integer>(parent, index, AttributeRelationType.LISTVALUE);
	}

	public static AttributeDescriptor<Integer> createListAccessor(Attribute<?> parent, int index) {
		return new AttributeDescriptor<Integer>(parent, index, AttributeRelationType.LISTVALUE_CONSTANT);
	}

	public static <KT> AttributeDescriptor<KT> createMapAccessor(Attribute<?> parent, Condition<KT> key) {
		return new AttributeDescriptor<KT>(parent, key, AttributeRelationType.MAPVALUE);
	}

	public static <KT> AttributeDescriptor<KT> createMapAccessor(Attribute<?> parent, KT key) {
		return new AttributeDescriptor<KT>(parent, key, AttributeRelationType.MAPVALUE_CONSTANT);
	}

	public static AttributeDescriptor<String> createPropertyAccessor(Attribute<?> parent, String property) {
		return new AttributeDescriptor<String>(parent, property, AttributeRelationType.PROPERTY);
	}

	public static AttributeDescriptor<String> createVariableAccessor(String variable) {
		return new AttributeDescriptor<String>(null, variable, AttributeRelationType.VARIABLE);
	}

	private AttributeDescriptorFactory() {
	}

}
