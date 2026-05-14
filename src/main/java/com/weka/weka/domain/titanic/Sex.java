package com.weka.weka.domain.titanic;

import java.util.Locale;

public enum Sex {

	MALE,
	FEMALE;

	public static Sex fromCsvValue(String value) {
		if (value == null) {
			return null;
		}
		return Sex.valueOf(value.trim().toUpperCase(Locale.ROOT));
	}

	public String toCsvValue() {
		return name().toLowerCase(Locale.ROOT);
	}
}