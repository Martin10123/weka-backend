# Flujo rapido de prueba en Postman

## 1. Validar dataset

`POST /datasets/validate`

Body: `form-data`

- `file`: `titanic.csv` o `titanic.arff`

Debe devolver si el archivo es valido y el formato detectado.

**Response:**
```json
{
  "valid": true,
  "format": "CSV",
  "message": "Dataset is valid"
}
```

## 2. Subir dataset

`POST /datasets/upload`

Body: `form-data`

- `file`: `titanic.csv` o `titanic.arff`

Devuelve nombre del archivo y cantidad total de filas.

**Response:**
```json
{
  "sourceFileName": "titanic.csv",
  "totalRows": 891
}
```

## 3. Previsualizar dataset

`GET /datasets/preview?limit=5`

Body: `form-data`

- `file`: `titanic.csv` o `titanic.arff`

Devuelve las primeras filas para revisar que el parseo quedó bien.

**Response:**
```json
{
  "rows": [
    {
      "age": 22.0,
      "passengerClass": "THIRD",
      "sex": "MALE",
      "travelingAlone": true,
      "embarked": "S",
      "survived": 0
    },
    {
      "age": 38.0,
      "passengerClass": "FIRST",
      "sex": "FEMALE",
      "travelingAlone": false,
      "embarked": "C",
      "survived": 1
    }
  ],
  "totalRows": 891
}
```

## 4. Normalizar dataset

`POST /datasets/normalize`

Body: `form-data`

- `file`: `titanic.csv` o `titanic.arff`

Devuelve filas limpias listas para entrenamiento.

**Response:**
```json
{
  "rows": [
    {
      "age": 22.0,
      "passengerClass": "THIRD",
      "sex": "MALE",
      "travelingAlone": true,
      "embarked": "S",
      "survived": 0
    }
  ],
  "usedRows": 891,
  "discardedRows": 0
}
```

## 5. Entrenar modelo J48

`POST /models/train`

Body: `form-data`

- `file`: `titanic.csv` o `titanic.arff`

Genera el `.model` en `data/models/` y devuelve `crossValidationAccuracy`, `summary` e `insight` con explicacion generada por IA (Cerebras). Si no hay `CEREBRAS_API_KEY` o la llamada falla, usa una explicacion local de respaldo.

**Response:**
```json
{
  "sourceFileName": "titanic.csv",
  "modelFilePath": "data\\models\\titanic-j48.model",
  "totalRows": 891,
  "usedRows": 891,
  "discardedRows": 0,
  "crossValidationAccuracy": 80.92031425364759,
  "insight": "1) Resultado: La exactitud del modelo J48 es del 80,92% con una kappa estadística de 0,5736, indicando una buena clasificación.\n2) Lectura: El error absoluto medio es del 27,14% y el error cuadrático medio raíz es del 37,51%, sugiriendo una precisión moderada.\n3) Mejora: Poda de características no relevantes y ajuste de minNumObj para mejorar la precisión y reducir el error absoluto.",
  "summary": "J48 cross-validation summary\nCorrectly Classified Instances         721               80.9203 %\nIncorrectly Classified Instances       170               19.0797 %\nKappa statistic                          0.5736\nMean absolute error                      0.2714\nRoot mean squared error                  0.3751\nRelative absolute error                 57.3765 %\nRoot relative squared error             77.1339 %\nTotal Number of Instances              891"
}
```

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

**Response:**
```json
{
  "survived": true,
  "probability": 0.9680851063829787,
  "rules": [
    "if sex = female AND passenger_class = FIRST then survived"
  ],
  "insight": "Prediction result: passenger survived with probability 97% . Selected rule: if sex = female AND passenger_class = FIRST then survived",
  "narrative": "Narrative will be added in the explanation phase."
}
```

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

**Response:**
```json
{
  "survived": true,
  "probability": 0.9680851063829787,
  "rules": [
    "if sex = female AND passenger_class = FIRST then survived"
  ],
  "narrative": "La noche del 14 de abril de 1912, la señora de alta sociedad, de 26 años, embarcó en el RMS Titanic desde Southampton, acompañada por un ser querido, con la ilusión de llegar a Nueva York en el más lujoso y moderno buque de la época. Su pertenencia a la clase alta y su género femenino la colocaron en una posición de privilegio, según las reglas del destino. Con una probabilidad del 97%, el modelo predijo que sobreviviría al catastrófico naufragio, y así fue. Su destino se vio influenciado por la combinación de su estatus social y su género, que le permitieron acceder a los pocos lugares disponibles en los botes salvavidas, asegurando su supervivencia en medio de la tragedia.",
  "provider": "cerebras"
}
```

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

**Response:**
```json
{
  "original": {
    "survived": true,
    "probability": 0.9680851063829787,
    "rules": [
      "if sex = female AND passenger_class = FIRST then survived"
    ],
    "insight": "Prediction result: passenger survived with probability 97% . Selected rule: if sex = female AND passenger_class = FIRST then survived",
    "narrative": "Narrative will be added in the explanation phase."
  },
  "modified": {
    "survived": false,
    "probability": 0.625,
    "rules": [
      "if sex = female AND passenger_class = THIRD AND embarked = S then not_survived"
    ],
    "insight": "Prediction result: passenger did not survive with probability 63% . Selected rule: if sex = female AND passenger_class = THIRD AND embarked = S then not_survived",
    "narrative": "Narrative will be added in the explanation phase."
  },
  "probabilityDelta": -0.34308510638297873,
  "summary": "Original profile would likely survive. Modified profile would likely not survive. Probability decrease of 34% points."
}
```

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