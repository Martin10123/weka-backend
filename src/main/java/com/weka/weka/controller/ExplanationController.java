package com.weka.weka.controller;

import com.weka.weka.contract.request.PassengerProfileRequest;
import com.weka.weka.contract.response.ExplanationResponse;
import com.weka.weka.service.TitanicExplanationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/explain")
public class ExplanationController {
	private final TitanicExplanationService titanicExplanationService;

	public ExplanationController(TitanicExplanationService titanicExplanationService) {
		this.titanicExplanationService = titanicExplanationService;
	}

	@PostMapping
	public ExplanationResponse explain(@RequestBody PassengerProfileRequest request) {
		return titanicExplanationService.explain(request);
	}
}