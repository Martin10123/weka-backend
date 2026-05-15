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

`POST /datasets/preview?limit=5`

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

---

## Sidebar aparte: Football Match Intelligence System

Esta sidebar es independiente de Titanic. Usa el modelo de fútbol para predecir `FTResult` y no reutiliza los endpoints de supervivencia.

## 1. Entrenar modelo de resultado

`POST /football/models/train`

Body: `form-data`

- `file`: `Matches.csv` o un `.arff` equivalente

Debe entrenar el RandomForest con estas columnas:

- `Division`
- `HomeElo`
- `AwayElo`
- `Form3Home`
- `Form3Away`
- `Form5Home`
- `Form5Away`
- `OddHome`
- `OddDraw`
- `OddAway`
- `FTResult`

**Response:**
```json
{
  "sourceFileName": "Matches.csv",
  "modelFilePath": "data\\models\\football-matches-randomforest.model",
  "totalRows": 200000,
  "usedRows": 198500,
  "discardedRows": 1500,
  "crossValidationAccuracy": 58.42,
  "insight": "Entrenamiento completado con ...",
  "summary": "RandomForest cross-validation summary ..."
}
```

## 2. Probar predicción

`POST /football/predict`

Body: `raw JSON`

```json
{
  "division": "F1",
  "homeElo": 1686.34,
  "awayElo": 1586.57,
  "form3Home": 0,
  "form3Away": 0,
  "form5Home": 0,
  "form5Away": 0,
  "homeOdds": 1.65,
  "drawOdds": 3.3,
  "awayOdds": 4.3
}
```

Devuelve la clase predicha (`Home Win`, `Draw` o `Away Win`) y las probabilidades por clase.

**Response:**
```json
{
  "predictedResult": "Home Win",
  "homeWinProbability": 0.62,
  "drawProbability": 0.21,
  "awayWinProbability": 0.17,
  "confidence": 0.62,
  "insight": "Prediccion: Home Win con 62.0% de confianza..."
}
```

## Orden recomendado para esta sidebar

1. `football/models/train`
2. `football/predict`

## Flujo sugerido para frontend

1. Sidebar de Titanic.
  - Mantiene el flujo original para supervivencia.
  - No comparte formularios ni respuestas con fútbol.

2. Sidebar de Football.
  - Un formulario para entrenar el modelo con `Matches.csv`.
  - Otro formulario independiente para predecir `FTResult`.
  - El usuario cambia de sidebar, no de modo dentro de la misma vista.

3. Separación visual.
  - Titanic muestra supervivencia, explicaciones y what-if.
  - Football muestra resultado del partido y probabilidades por clase.
  - Cada sidebar conserva su propio contexto y sus propios botones.

## Flujo sugerido para frontend

Este es el orden recomendado para la UI del frontend, para que el usuario no se pierda y el sistema tenga siempre el contexto necesario:

1. Seleccionar y validar archivo.
  - El usuario elige `titanic.csv` o `titanic.arff`.
  - El frontend llama a `POST /datasets/validate`.
  - Si el archivo no es valido, se muestra el error y no se habilita el siguiente paso.

2. Subir dataset.
  - Cuando la validacion es correcta, el frontend llama a `POST /datasets/upload`.
  - Se debe mostrar `sourceFileName` y `totalRows`.
  - En esta etapa ya se puede guardar el dataset como base activa de la sesion.

3. Previsualizar los datos.
  - El frontend llama a `GET /datasets/preview?limit=5`.
  - Se muestran algunas filas en una tabla para confirmar que el parseo fue correcto.
  - Si la vista previa no coincide con lo esperado, el usuario puede volver a cargar otro archivo.

4. Normalizar dataset.
  - El frontend llama a `POST /datasets/normalize`.
  - Se muestran `usedRows`, `discardedRows` y una muestra de filas limpias.
  - Esta vista sirve para confirmar que los datos estan listos para entrenamiento.

5. Entrenar modelo.
  - El frontend llama a `POST /models/train`.
  - Se muestran `crossValidationAccuracy`, `summary` e `insight`.
  - Tambien conviene mostrar el estado de generacion del modelo y la ruta del archivo `.model`.

6. Hacer prediccion.
  - Con el modelo ya entrenado, el usuario completa el formulario de perfil del pasajero.
  - El frontend llama a `POST /predict`.
  - Se muestran `survived`, `probability`, `rules`, `insight` y `narrative`.

7. Generar explicacion narrativa.
  - Si el usuario quiere una narrativa mas detallada, el frontend llama a `POST /explain`.
  - Este paso depende de que exista el modelo entrenado y de que `CEREBRAS_API_KEY` este configurada.
  - Si el backend responde con fallback local, el frontend debe mostrarlo como explicacion alternativa.

8. Ejecutar simulacion what-if.
  - El frontend reutiliza el mismo perfil base y cambia un solo campo.
  - Luego llama a `POST /what-if`.
  - Se comparan `original`, `modified` y `probabilityDelta` para mostrar el impacto del cambio.

### Flujo resumido de UI

- Paso 1: cargar archivo.
- Paso 2: validar.
- Paso 3: subir.
- Paso 4: previsualizar.
- Paso 5: normalizar.
- Paso 6: entrenar.
- Paso 7: predecir.
- Paso 8: explicar.
- Paso 9: probar escenarios alternativos con what-if.

### Reglas practicas para el frontend

- No habilitar prediccion, explicacion o what-if hasta haber entrenado el modelo.
- Si falla `predict`, `explain` o `what-if`, mostrar primero un aviso de que debe ejecutarse `POST /models/train`.
- Mantener visible el ultimo archivo cargado y el estado actual del proceso.
- Mostrar mensajes claros de exito y error en cada paso.

## Nota

Si `predict`, `explain` o `what-if` fallan por falta de modelo, corre primero `POST /models/train`.