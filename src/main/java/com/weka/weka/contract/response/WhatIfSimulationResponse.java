package com.weka.weka.contract.response;

public record WhatIfSimulationResponse(
		PredictionResponse original,
		PredictionResponse modified,
		double probabilityDelta,
		String summary
) {
}