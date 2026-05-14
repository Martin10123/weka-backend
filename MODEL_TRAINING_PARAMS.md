# Titanic J48 Training Parameters

## Modelo

- Classifier: J48
- Confidence factor: 0.25
- Minimum number of objects per leaf: 2
- Cross-validation folds: 10
- Random seed: 1

## Salida

- Modelo serializado en `data/models/<dataset-name>-j48.model`
- Artefacto serializado incluye el clasificador y la estructura del dataset

## Criterio

- Entrenar sobre el dataset ya normalizado.
- Excluir filas que no puedan normalizarse a un esquema utilizable.
- Mantener la misma estructura de atributos para entrenamiento y prediccion futura.