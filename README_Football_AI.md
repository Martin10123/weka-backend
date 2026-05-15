# ⚽ Football Match Intelligence System

## 📌 Descripción

Aplicación web basada en Machine Learning que predice resultados de partidos de fútbol utilizando la API de Weka en Java.  

El sistema integra múltiples funcionalidades en una sola plataforma tipo dashboard.

---

## 🎯 Objetivo del Proyecto

Desarrollar una aplicación que permita:

- Predecir el resultado de un partido
- Estimar la cantidad de goles (Over/Under)
- Analizar la forma de los equipos
- Detectar oportunidades de apuestas
- Simular temporadas completas

---

## 📊 Dataset

Dataset utilizado:

https://github.com/xgabora/Club-Football-Match-Data-2000-2025

Contiene:
- +200,000 partidos
- Elo ratings
- Resultados históricos
- Odds de apuestas
- Estadísticas de rendimiento

---

## 🧠 Enfoque de Machine Learning

Aunque el sistema tiene 5 módulos, realmente se basa en:

> 🎯 **2 modelos principales + lógica adicional**

---

## 🔵 Modelo 1: Predicción de Resultado

- Tipo: Clasificación
- Target: `FTResult`
- Clases:
  - Home Win
  - Draw
  - Away Win

### Features:
- HomeElo
- AwayElo
- Form3Home
- Form3Away
- Form5Home
- Form5Away
- Odds de apuesta:
  - OddHome
  - OddDraw
  - OddAway
- División

### Algoritmo:
- RandomForest

---

## 🔴 Modelo 2: Over / Under 2.5 goles

- Tipo: Clasificación
- Target: `Over25`

### Features:
- Goles recientes
- Forma de equipos
- Elo
- Estadísticas ofensivas

### Algoritmo:
- RandomForest

---

## 🧩 Módulos del Sistema

---

### 1. ⚽ Predictor de Partidos

Predice el resultado de un partido.

**Entrada:**
- Equipo local
- Equipo visitante

**Salida:**
- Probabilidad de victoria local
- Empate
- Victoria visitante

👉 Usa: Modelo 1

---

### 2. 🔥 Over / Under

Predice si habrá más de 2.5 goles.

**Salida:**
- Probabilidad Over
- Probabilidad Under

👉 Usa: Modelo 2

---

### 3. 📈 Análisis de Rachas

Evalúa el estado del equipo.

**Método:**
- Últimos 5 partidos
- % victorias
- Goles anotados
- Diferencia de goles

**Salida:**
- Buena forma
- Forma media
- Mala forma

👉 No requiere modelo complejo

---

### 4. 💰 Recomendador de Apuestas

Compara:

- Probabilidad del modelo
- Probabilidad implícita de odds

**Ejemplo:**
- Modelo: 70%
- Casa apuestas: 55%

👉 Detecta apuestas de valor

👉 Usa: Modelo 1 + lógica

---

### 5. 🏆 Simulador de Temporada

Simula una liga completa.

**Proceso:**
1. Predice cada partido
2. Genera resultados
3. Calcula tabla
4. Repite múltiples veces

**Salida:**
- Probabilidad de campeón
- Posiciones finales

👉 Usa: Modelo 1 repetidamente

---

## 🏗️ Arquitectura

### Backend
- Java
- Spring Boot
- Weka API

### Frontend
- React o HTML + JS
- Sidebar interactivo

---

## 📡 Endpoints

### `POST /football/predict`

Predice `FTResult` con el modelo 1.

Body esperado:

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

### `POST /football/models/train`

Entrena el RandomForest con un CSV o ARFF con estas columnas:

- Division
- HomeElo
- AwayElo
- Form3Home
- Form3Away
- Form5Home
- Form5Away
- OddHome
- OddDraw
- OddAway
- FTResult
