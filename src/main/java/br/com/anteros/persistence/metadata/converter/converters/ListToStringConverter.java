package br.com.anteros.persistence.metadata.converter.converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.metadata.annotation.Converter;
import br.com.anteros.persistence.metadata.converter.AttributeConverter;

@Converter
public class ListToStringConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "";
        }
        return StringUtils.join( ",",attribute.toArray(new String[]{}));
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().length() == 0) {
            return new ArrayList<String>();
        }

        String[] data = dbData.split(",");
        return Arrays.asList(data);
    }
}
