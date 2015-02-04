package br.com.anteros.persistence.metadata.converter.converters;

import java.util.Date;

import org.joda.time.Instant;

import br.com.anteros.persistence.metadata.annotation.Converter;
import br.com.anteros.persistence.metadata.converter.AttributeConverter;


@Converter(autoApply = true)
public class JodaInstantConverter implements AttributeConverter<Instant, Date> {

    public Date convertToDatabaseColumn(Instant instant) {
        return instant.toDate();
    }

    public Instant convertToEntityAttribute(Date date) {
        return new Instant(date);
    }
}
