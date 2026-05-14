# Plan de Backend MVP - Titanic + Weka

## Alcance
Este plan cubre solo lo necesario para el MVP descrito en `README_titanic_mvp.md`:

- Prediccion de supervivencia con Weka.
- Explicacion narrativa basada en reglas del modelo.
- Mini simulador what-if para comparar cambios simples.

El proyecto ya esta montado sobre Spring Boot, asi que el plan se ejecuta sobre esta base y no sobre Flask.

## Estado actual

- Proyecto Java/Spring Boot vacio a nivel funcional.
- Existe solo la clase principal de arranque.
- No hay endpoints, servicios, repositorios, dataset ni modelo entrenado.

## Objetivo del backend
Construir un backend minimo que reciba el perfil de un pasajero del Titanic, ejecute una prediccion con Weka y devuelva una respuesta util para el frontend en menos de 10 segundos.

## Plan de trabajo

### Fase 1: Definir contrato de datos
Definir el modelo de entrada y salida del backend.

Entrada minima:

- sexo
- edad
- clase del pasaje
- viajaba solo
- puerto de embarque

Salida minima:

- survived
- probability
- rules
- insight
- narrative

### Fase 2: Preparar el dataset
Limpiar y normalizar el Titanic CSV para que use el mismo esquema que el modelo.

Tareas:

- identificar columnas utiles
- tratar valores nulos
- convertir categorias a un formato compatible con Weka
- separar atributos de entrada y variable objetivo

### Fase 3: Entrenar el modelo MVP
Entrenar un modelo interpretable con Weka, usando J48 como primera opcion.

Tareas:

- crear el script o proceso de entrenamiento
- evaluar con cross-validation
- exportar el archivo `.model`
- guardar reglas relevantes del arbol

### Fase 4: Implementar `/predict`
Crear un endpoint que:

- reciba el perfil del pasajero
- transforme la entrada al formato del modelo
- cargue el modelo entrenado
- ejecute la prediccion
- devuelva veredicto y probabilidad

### Fase 5: Implementar extraccion de reglas
Extraer reglas simples del modelo para explicar por que se toma una decision.

Salida esperada:

- lista corta de reglas del arbol
- reglas asociadas a la prediccion actual

### Fase 6: Implementar `/explain`
Crear un endpoint que use las reglas y la prediccion para generar una narrativa historica con Claude.

Tareas:

- construir el prompt con contexto real del Titanic
- enviar resultado, probabilidad y reglas
- devolver una explicacion breve, coherente y no inventada

### Fase 7: Mini simulador what-if
Permitir cambiar una variable para ver como cambia la probabilidad.

Ejemplo:

- cambiar clase del pasaje
- comparar probabilidad original vs probabilidad nueva

### Fase 8: Validacion y cierre
Verificar que el flujo completo funciona.

Checklist:

- el backend arranca sin errores
- `/predict` responde correctamente
- `/explain` responde correctamente
- el simulador what-if devuelve una comparacion util
- el tiempo total se mantiene dentro del objetivo del MVP

## Entregables

- API REST funcional para prediccion.
- Integracion con modelo Weka.
- Integracion con API de Claude para narrativa.
- Documentacion minima de uso.
- Modelo entrenado en archivo `.model`.

## Fuera de alcance

- login y autenticacion
- base de datos de usuarios
- historial de predicciones
- panel administrativo
- soporte para varios modelos en produccion
- optimizacion avanzada de infraestructura

## Orden recomendado
1. Definir contrato de datos.
2. Preparar dataset.
3. Entrenar y exportar modelo.
4. Implementar `/predict`.
5. Implementar extraccion de reglas.
6. Implementar `/explain`.
7. Implementar what-if.
8. Validar y documentar.

## Proximo paso
Si este plan te sirve, el siguiente paso es crear la estructura real del backend dentro del proyecto: controller, service, dto, integracion con Weka y la base para Claude.