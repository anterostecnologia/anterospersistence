package br.com.anteros.persistence.osql.attribute;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class Constants {
    
    private static final Set<Class<?>> typedClasses = new HashSet<Class<?>>(Arrays.<Class<?>>asList(
            ArrayAttribute.class,
            AttributeBuilder.class,
            ComparableAttribute.class,
            EnumAttribute.class,
            DateAttribute.class,
            DateTimeAttribute.class,
            BeanAttribute.class,
            EntityAttributeBase.class,
            NumberAttribute.class,
            SimpleAttribute.class,
            TimeAttribute.class
            ));
    
    public static boolean isTyped(Class<?> cl) {
        return typedClasses.contains(cl);
    }
    
    private Constants() {}

}
