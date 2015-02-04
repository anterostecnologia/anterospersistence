package br.com.anteros.persistence.metadata.converter.converters;

import org.joda.time.Interval;

import br.com.anteros.persistence.metadata.annotation.Converter;
import br.com.anteros.persistence.metadata.converter.AttributeConverter;

/**
 * Converts a Joda Interval <-> JPA 2.1
 *
 * An Interval is a complex temporal type, maintaining the start and end
 * instants and deriving the period of time between the two instants. Converting
 * this type into a JPA/SQL time is not possible using a simple Converter (can
 * only serialize to a single field), so we do our best here by serializing the
 * Interval as a String.
 */
@Converter
public class JodaIntervalConverter implements AttributeConverter<Interval, String> {

    public String convertToDatabaseColumn(Interval interval) {
        return interval.toString();
    }

    public Interval convertToEntityAttribute(String string) {
        return Interval.parse(string);
    }
}
