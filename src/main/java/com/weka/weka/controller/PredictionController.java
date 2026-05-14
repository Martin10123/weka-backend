package com.weka.weka.controller;

import com.weka.weka.contract.request.PassengerProfileRequest;
import com.weka.weka.contract.response.PredictionResponse;
import com.weka.weka.service.TitanicPredictionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/predict")
public class PredictionController {
	private final TitanicPredictionService titanicPredictionService;

	public PredictionController(TitanicPredictionService titanicPredictionService) {
		this.titanicPredictionService = titanicPredictionService;
	}

	@PostMapping
	public PredictionResponse predict(@RequestBody PassengerProfileRequest request) {
		return titanicPredictionService.predict(request);
	}
}