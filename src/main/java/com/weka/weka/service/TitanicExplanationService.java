package com.weka.weka.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weka.weka.contract.request.PassengerProfileRequest;
import com.weka.weka.contract.response.ExplanationResponse;
import com.weka.weka.contract.response.PredictionResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TitanicExplanationService {
	private final TitanicPredictionService titanicPredictionService;
	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;
	private final String cerebrasApiKey;
	private final String cerebrasBaseUrl;
	private final String cerebrasModel;

	public TitanicExplanationService(
			TitanicPredictionService titanicPredictionService,
			ObjectMapper objectMapper,
			@Value("${cerebras.api-key:}") String cerebrasApiKey,
			@Value("${cerebras.base-url:https://api.cerebras.ai/v1}") String cerebrasBaseUrl,
			@Value("${cerebras.model:llama3.1-8b}") String cerebrasModel
	) {
		this.titanicPredictionService = titanicPredictionService;
		this.objectMapper = objectMapper;
		this.cerebrasApiKey = cerebrasApiKey;
		this.cerebrasBaseUrl = cerebrasBaseUrl;
		this.cerebrasModel = cerebrasModel;
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.build();
	}

	public ExplanationResponse explain(PassengerProfileRequest request) {
		PredictionResponse prediction = titanicPredictionService.predict(request);
		String narrative = generateNarrative(request, prediction);
		return new ExplanationResponse(
				prediction.survived(),
				prediction.probability(),
				prediction.rules(),
				narrative,
				"cerebras"
		);
	}

	public String explainTrainingResult(
			String sourceFileName,
			long totalRows,
			long usedRows,
			int discardedRows,
			double crossValidationAccuracy,
			String summary,
			String fallbackInsight
	) {
		if (cerebrasApiKey == null || cerebrasApiKey.isBlank()) {
			return fallbackInsight;
		}

		try {
			Map<String, Object> payload = new LinkedHashMap<>();
			payload.put("model", cerebrasModel);
			payload.put("max_completion_tokens", 180);
			payload.put("temperature", 0.2);
			payload.put("top_p", 1);
			payload.put("stream", false);
			payload.put("messages", buildTrainingMessages(sourceFileName, totalRows, usedRows, discardedRows, crossValidationAccuracy, summary));

			String requestBody = objectMapper.writeValueAsString(payload);
			HttpRequest httpRequest = HttpRequest.newBuilder()
					.uri(URI.create(cerebrasBaseUrl + "/chat/completions"))
					.timeout(Duration.ofSeconds(30))
					.header("Authorization", "Bearer " + cerebrasApiKey)
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(requestBody))
					.build();

			HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				return fallbackInsight;
			}

			JsonNode root = objectMapper.readTree(response.body());
			JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
			if (contentNode.isMissingNode() || contentNode.isNull()) {
				return fallbackInsight;
			}

			String aiInsight = contentNode.asText().trim();
			return aiInsight.isBlank() ? fallbackInsight : aiInsight;
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			return fallbackInsight;
		} catch (IOException exception) {
			return fallbackInsight;
		}
	}

	private String generateNarrative(PassengerProfileRequest request, PredictionResponse prediction) {
		if (cerebrasApiKey == null || cerebrasApiKey.isBlank()) {
			throw new IllegalStateException("CEREBRAS_API_KEY environment variable is required to generate explanations");
		}

		try {
			Map<String, Object> payload = new LinkedHashMap<>();
			payload.put("model", cerebrasModel);
			payload.put("max_completion_tokens", 512);
			payload.put("temperature", 0.2);
			payload.put("top_p", 1);
			payload.put("stream", false);
			payload.put("messages", buildMessages(request, prediction));

			String requestBody = objectMapper.writeValueAsString(payload);
			HttpRequest httpRequest = HttpRequest.newBuilder()
					.uri(URI.create(cerebrasBaseUrl + "/chat/completions"))
					.timeout(Duration.ofSeconds(30))
					.header("Authorization", "Bearer " + cerebrasApiKey)
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(requestBody))
					.build();

			HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new IllegalStateException("Cerebras request failed with status " + response.statusCode() + ": " + response.body());
			}

			JsonNode root = objectMapper.readTree(response.body());
			JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
			if (contentNode.isMissingNode() || contentNode.isNull()) {
				throw new IllegalStateException("Cerebras response did not include message content");
			}

			return contentNode.asText().trim();
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Cerebras request was interrupted", exception);
		} catch (IOException exception) {
			throw new IllegalStateException("Unable to generate Titanic narrative", exception);
		}
	}

	private List<Map<String, String>> buildMessages(PassengerProfileRequest request, PredictionResponse prediction) {
		List<Map<String, String>> messages = new ArrayList<>();
		messages.add(message("system", "Eres un historiador del Titanic. Explica la prediccion sin inventar causas externas. Usa solo el perfil del pasajero y las reglas del modelo. Responde en 3 a 4 oraciones, en tono historico y claro."));
		messages.add(message("user", buildUserPrompt(request, prediction)));
		return messages;
	}

	private List<Map<String, String>> buildTrainingMessages(
			String sourceFileName,
			long totalRows,
			long usedRows,
			int discardedRows,
			double crossValidationAccuracy,
			String summary
	) {
		List<Map<String, String>> messages = new ArrayList<>();
		messages.add(message("system", "Eres un analista senior de machine learning. Responde en espanol tecnico y conciso. Usa solo los datos recibidos. No inventes variables ni recomiendes recolectar mas datos. Nunca expreses MAE/RMSE como porcentaje: reportalos como error medio (0-1). Si accuracy esta entre 75% y 85%, describela como precision moderada/aceptable, no alta. Salida obligatoria en exactamente 3 lineas, cada una iniciando con: 1) Resultado:, 2) Lectura:, 3) Mejora:. Cada linea maximo 22 palabras."));
		messages.add(message("user", "Fuente: " + sourceFileName + "\n"
				+ "Total de filas: " + totalRows + "\n"
				+ "Filas usadas: " + usedRows + "\n"
				+ "Filas descartadas: " + discardedRows + "\n"
				+ "Exactitud CV (%): " + String.format("%.2f", crossValidationAccuracy) + "\n"
				+ "Resumen Weka:\n" + summary + "\n"
				+ "Tarea: interpreta el entrenamiento J48 con foco en accuracy, kappa y errores. Menciona MAE/RMSE en escala de error medio. Recomienda solo ajustes de modelo/proceso (poda, minNumObj, validacion, limpieza/encoding)."));
		return messages;
	}

	private Map<String, String> message(String role, String content) {
		Map<String, String> message = new LinkedHashMap<>();
		message.put("role", role);
		message.put("content", content);
		return message;
	}

	private String buildUserPrompt(PassengerProfileRequest request, PredictionResponse prediction) {
		return "Perfil del pasajero: " + request + "\n"
				+ "Resultado del modelo: " + (prediction.survived() ? "survived" : "not_survived") + "\n"
				+ "Probabilidad: " + Math.round(prediction.probability() * 100.0) + "%\n"
				+ "Reglas del modelo: " + String.join(" | ", prediction.rules()) + "\n"
				+ "Tarea: redacta una narrativa breve que explique por que esta persona habria tenido ese destino.";
	}
}