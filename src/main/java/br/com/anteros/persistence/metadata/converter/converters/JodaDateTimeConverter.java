package br.com.anteros.persistence.metadata.converter.converters;

import java.util.Date;

import org.joda.time.DateTime;

import br.com.anteros.persistence.metadata.annotation.Converter;
import br.com.anteros.persistence.metadata.converter.AttributeConverter;


@Converter(autoApply = true)
public class JodaDateTimeConverter implements AttributeConverter<DateTime, Date> {

    public Date convertToDatabaseColumn(DateTime dateTime) {
        return dateTime.toDate();
    }

    public DateTime convertToEntityAttribute(Date date) {
        return new DateTime(date);
    }
}
