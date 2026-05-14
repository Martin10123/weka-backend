package com.weka.weka.domain.titanic;

import java.util.Locale;

public enum EmbarkationPort {

	SOUTHAMPTON("S"),
	CHERBOURG("C"),
	QUEENSTOWN("Q");

	private final String csvValue;

	EmbarkationPort(String csvValue) {
		this.csvValue = csvValue;
	}

	public String getCsvValue() {
		return csvValue;
	}

	public static EmbarkationPort fromCsvValue(String value) {
		if (value == null) {
			return null;
		}
		String normalizedValue = value.trim().toUpperCase(Locale.ROOT);
		for (EmbarkationPort port : values()) {
			if (port.csvValue.equals(normalizedValue)) {
				return port;
			}
		}
		throw new IllegalArgumentException("Unknown embarkation port: " + value);
	}
}