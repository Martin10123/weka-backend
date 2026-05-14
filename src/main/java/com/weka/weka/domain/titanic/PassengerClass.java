package com.weka.weka.domain.titanic;

public enum PassengerClass {

	FIRST(1),
	SECOND(2),
	THIRD(3);

	private final int csvValue;

	PassengerClass(int csvValue) {
		this.csvValue = csvValue;
	}

	public int getCsvValue() {
		return csvValue;
	}

	public static PassengerClass fromCsvValue(int value) {
		for (PassengerClass passengerClass : values()) {
			if (passengerClass.csvValue == value) {
				return passengerClass;
			}
		}
		throw new IllegalArgumentException("Unknown passenger class: " + value);
	}
}