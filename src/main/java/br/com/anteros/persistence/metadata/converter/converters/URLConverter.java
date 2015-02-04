package br.com.anteros.persistence.metadata.converter.converters;

import java.net.MalformedURLException;
import java.net.URL;

import br.com.anteros.persistence.metadata.converter.AttributeConverter;

public class URLConverter implements AttributeConverter<URL, String> {
	public URL convertToEntityAttribute(String str) {
		if (str == null) {
			return null;
		}

		URL url = null;
		try {
			url = new java.net.URL(str.trim());
		} catch (MalformedURLException mue) {
			throw new IllegalStateException("Erro convertendo para URL", mue);
		}
		return url;
	}

	public String convertToDatabaseColumn(URL url) {
		return url != null ? url.toString() : null;
	}
}