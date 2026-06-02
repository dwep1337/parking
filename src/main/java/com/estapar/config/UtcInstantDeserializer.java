package com.estapar.config;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
import tools.jackson.databind.exc.InvalidFormatException;

public class UtcInstantDeserializer extends StdScalarDeserializer<Instant> {

	public UtcInstantDeserializer() {
		super(Instant.class);
	}

	@Override
	public Instant deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
		String value = parser.getString();
		if (value == null || value.isBlank()) {
			return null;
		}

		try {
			return Instant.parse(value);
		}
		catch (DateTimeParseException ignored) {
			try {
				return LocalDateTime.parse(value).atZone(ZoneOffset.UTC).toInstant();
			}
			catch (DateTimeParseException ex) {
				throw InvalidFormatException.from(parser, "Invalid instant: " + value, value, Instant.class);
			}
		}
	}

}
