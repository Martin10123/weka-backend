# 🚢 ¿Habrías Sobrevivido al Titanic? — MVP PRO

## 🎯 Objetivo
Aplicación web interactiva que permite al usuario ingresar su perfil como pasajero del Titanic, predecir su probabilidad de supervivencia mediante un modelo de Machine Learning (Weka), y generar una explicación narrativa inteligente basada en IA.

El sistema combina:
- Predicción estadística real
- Explicación interpretativa del modelo
- Narrativa histórica generada por IA

---

## 🧠 Propuesta de Valor

> No solo predice — **explica y narra decisiones históricas con IA**

A diferencia de soluciones tradicionales, esta app:
- Traduce resultados técnicos en lenguaje natural
- Combina reglas del modelo con storytelling
- Permite explorar cómo pequeños cambios afectan el destino

---

## 🧱 Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Frontend | HTML + CSS + JS (vanilla) |
| Backend | Python Flask |
| Modelo predictivo | Weka (J48 + RandomForest) |
| IA narrativa | API Claude (Anthropic) |
| Dataset | Titanic (CSV) |

---

## ⚙️ Flujo del Sistema

```
[Formulario web]
      ↓
[POST /predict → Flask + Weka]
      ↓
{
  survived: true/false,
  probability: 0.82,
  rules: ["sex=female", "pclass=1"]
}
      ↓
[POST /explain → Claude API]
      ↓
[Resultado + narrativa + insights]
```

---

## 🖥️ Pantallas

### 1. Formulario — "Tu perfil en 1912"

Campos:
- Sexo (Hombre / Mujer)
- Edad (número)
- Clase del pasaje (1ra, 2da, 3ra)
- ¿Viajabas solo? (Sí / No)
- Puerto de embarque (Southampton, Cherbourg, Queenstown)

Botón:
👉 **"Descubrir mi destino"**

---

### 2. Resultado

Incluye:

- 🎯 Veredicto visual:
  - **SOBREVIVISTE** / **NO SOBREVIVISTE**

- 📊 Probabilidad:
  - Ej: 82%

- 📖 Narrativa IA (3–4 oraciones, tono histórico)

- 🧠 Insight del dataset:
  - Ej:  
    > “Personas con tu perfil tuvieron un 74% de tasa de supervivencia.”

- 🔄 Mini simulación:
  - Botón:  
    👉 “¿Qué pasaría si cambiaras de clase?”

---

## 🤖 Modelos en Weka

| Modelo | Descripción | Uso |
|---|---|---|
| **J48 (C4.5)** | Árbol de decisión | Interpretabilidad (MVP) |
| **RandomForest** | Ensemble | Mayor precisión (producción) |
| **Naive Bayes** | Probabilístico | Rápido, baseline |
| **Logistic** | Regresión logística | Probabilidades directas |

---

## 🧪 Recomendación de uso

- MVP → **J48**
- Producción → **RandomForest**

---

## ⚙️ Configuración sugerida

```bash
# J48
weka.classifiers.trees.J48 -C 0.25 -M 2

# RandomForest
weka.classifiers.trees.RandomForest -I 100 -K 0 -S 1
```

---

## 🧠 Explicabilidad del Modelo

El sistema no depende únicamente de IA generativa.

👉 Se extraen reglas reales del modelo:

Ejemplo:

```
if sex = female AND pclass = 1 → survived
```

Estas reglas se envían a la IA para generar explicaciones coherentes y fundamentadas.

---

## ✍️ Prompt Mejorado para Claude

```
El usuario tiene este perfil:
- Sexo: {sexo}
- Edad: {edad}
- Clase: {clase}
- Puerto: {puerto}
- Viaje: {solo/acompañado}

El modelo predice:
- Resultado: {sobrevive / no sobrevive}
- Probabilidad: {X}%

Reglas del modelo:
{lista de reglas del árbol J48}

Tarea:
Explica en 3-4 oraciones, en tono histórico y dramático,
por qué esta persona habría tenido ese destino.

IMPORTANTE:
- Basa la explicación en las reglas del modelo
- Usa contexto real del Titanic
- No inventes causas fuera de los datos
```

---

## 🧩 Backend (Flask)

### Tareas

- [ ] Preprocesar dataset
- [ ] Entrenar modelo en Weka
- [ ] Exportar modelo `.model`
- [ ] Evaluar con cross-validation
- [ ] Implementar `/predict`
- [ ] Extraer reglas
- [ ] Implementar `/explain`
- [ ] Integrar Claude API

---

## 🔁 Funcionalidad Extra: What-if Simulator

Permite modificar variables:

> “Subir a primera clase aumenta tu probabilidad en un 35%”

---

## ✅ Criterio de Éxito

> Predicción + explicación clara en menos de 10 segundos.

---

## 🏆 Pitch

> “Transformamos predicciones en historias comprensibles usando IA.”
