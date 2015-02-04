package br.com.anteros.persistence.metadata.converter.converters;

import java.util.Date;

import org.joda.time.LocalDate;

import br.com.anteros.persistence.metadata.annotation.Converter;
import br.com.anteros.persistence.metadata.converter.AttributeConverter;


@Converter(autoApply = true)
public class JodaLocalDateConverter implements AttributeConverter<LocalDate, Date> {

    public Date convertToDatabaseColumn(LocalDate localDate) {
        return localDate.toDate();
    }

    public LocalDate convertToEntityAttribute(Date date) {
        return LocalDate.fromDateFields(date);
    }
}
