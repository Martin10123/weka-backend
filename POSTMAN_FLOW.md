# Flujo rapido de prueba en Postman

## 1. Validar dataset

`POST /datasets/validate`

Body: `form-data`

- `file`: `titanic.csv` o `titanic.arff`

Debe devolver si el archivo es valido y el formato detectado.

## 2. Subir dataset

`POST /datasets/upload`

Body: `form-data`

- `file`: `titanic.csv` o `titanic.arff`

Devuelve nombre del archivo y cantidad total de filas.

## 3. Previsualizar dataset

`GET /datasets/preview?limit=5`

Body: `form-data`

- `file`: `titanic.csv` o `titanic.arff`

Devuelve las primeras filas para revisar que el parseo quedó bien.

## 4. Normalizar dataset

`POST /datasets/normalize`

Body: `form-data`

- `file`: `titanic.csv` o `titanic.arff`

Devuelve filas limpias listas para entrenamiento.

## 5. Entrenar modelo J48

`POST /models/train`

Body: `form-data`

- `file`: `titanic.csv` o `titanic.arff`

Genera el `.model` en `data/models/` y devuelve `crossValidationAccuracy`, `summary` e `insight` con explicacion generada por IA (Cerebras). Si no hay `CEREBRAS_API_KEY` o la llamada falla, usa una explicacion local de respaldo.

## 6. Probar prediccion

`POST /predict`

Body: `raw JSON`

```json
{
  "sex": "FEMALE",
  "age": 26,
  "passengerClass": "FIRST",
  "travelingAlone": false,
  "embarked": "SOUTHAMPTON"
}
```

Devuelve `survived`, `probability`, `rules`, `insight` y `narrative`.

## 7. Probar explicacion

Antes de llamar este endpoint, define la variable de entorno `CEREBRAS_API_KEY` en tu sistema.

`POST /explain`

Body: `raw JSON`

```json
{
  "sex": "FEMALE",
  "age": 26,
  "passengerClass": "FIRST",
  "travelingAlone": false,
  "embarked": "SOUTHAMPTON"
}
```

Devuelve la narrativa generada por Cerebras.

## 8. Probar what-if

`POST /what-if`

Body: `raw JSON`

```json
{
  "baseProfile": {
    "sex": "FEMALE",
    "age": 26,
    "passengerClass": "FIRST",
    "travelingAlone": false,
    "embarked": "SOUTHAMPTON"
  },
  "passengerClass": "THIRD"
}
```

Devuelve la prediccion original, la modificada y la diferencia de probabilidad.

## Orden recomendado

1. `validate`
2. `upload`
3. `preview`
4. `normalize`
5. `models/train`
6. `predict`
7. `explain`
8. `what-if`

## Nota

Si `predict`, `explain` o `what-if` fallan por falta de modelo, corre primero `POST /models/train`.