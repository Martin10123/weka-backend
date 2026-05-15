package com.weka.weka.controller;

import com.weka.weka.contract.request.FootballMatchPredictionRequest;
import com.weka.weka.contract.response.FootballMatchPredictionResponse;
import com.weka.weka.contract.response.ModelTrainingResponse;
import com.weka.weka.service.FootballMatchModelTrainingService;
import com.weka.weka.service.FootballMatchPredictionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/football")
public class FootballController {
	private final FootballMatchPredictionService footballMatchPredictionService;
	private final FootballMatchModelTrainingService footballMatchModelTrainingService;

	public FootballController(
			FootballMatchPredictionService footballMatchPredictionService,
			FootballMatchModelTrainingService footballMatchModelTrainingService
	) {
		this.footballMatchPredictionService = footballMatchPredictionService;
		this.footballMatchModelTrainingService = footballMatchModelTrainingService;
	}

	@PostMapping("/predict")
	public FootballMatchPredictionResponse predict(@RequestBody FootballMatchPredictionRequest request) {
		return footballMatchPredictionService.predict(request);
	}

	@PostMapping(value = "/models/train", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ModelTrainingResponse train(@RequestParam("file") MultipartFile file) {
		return footballMatchModelTrainingService.train(file);
	}
}