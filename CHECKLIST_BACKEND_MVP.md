# Checklist Backend MVP - Titanic + Weka

## Estado general

- [x] Proyecto base Spring Boot identificado.
- [x] Alcance MVP definido a partir del README.
- [x] Contrato de dominio inicial definido.
- [x] Lectura de dataset desde archivo implementada.
- [x] Soporte para CSV y ARFF agregado.
- [x] Endpoint de upload del dataset creado.
- [x] Endpoint de preview del dataset creado.
- [x] Endpoint de validacion del dataset creado.
- [x] Endpoint de normalizacion del dataset creado.
- [x] Normalizacion base del dataset implementada.
- [x] Entrenamiento del modelo Weka J48.
- [x] Exportacion del archivo `.model`.
- [x] Endpoint `/predict`.
- [x] Extraccion de reglas del modelo.
- [x] Endpoint `/explain` con Claude.
- [x] Simulador what-if.
- [ ] Validacion final del flujo completo.

## Fase 1: Definir contrato de datos

- [x] Definir entrada minima del pasajero.
- [x] Definir salida minima del backend.
- [x] Crear enums de dominio para sexo, clase y puerto.
- [x] Crear estructura para una fila Titanic.
- [x] Crear response base para prediccion.

## Fase 2: Preparar el dataset

- [x] Detectar que el dataset puede subir como archivo.
- [x] Leer CSV con columnas reales del Titanic.
- [x] Leer ARFF con Weka.
- [x] Mapear cada fila a una estructura interna comun.
- [x] Validar esquema exacto del archivo subido.
- [x] Limpiar nulos y valores inconsistentes.
- [x] Normalizar columnas para entrenamiento.
- [x] Separar features y objetivo.

## Fase 3: Entrenar el modelo MVP

- [x] Definir proceso de entrenamiento reproducible.
- [x] Elegir y entrenar J48 como modelo principal.
- [x] Ejecutar cross-validation.
- [x] Guardar el modelo exportado.
- [x] Documentar parametros de entrenamiento.

## Fase 4: Implementar `/predict`

- [x] Crear servicio de carga de modelo.
- [x] Crear transformacion de entrada a instancias Weka.
- [x] Crear endpoint de prediccion.
- [x] Devolver sobrevivio y probabilidad.
- [x] Devolver reglas base del arbol.

## Fase 5: Implementar extraccion de reglas

- [x] Extraer reglas desde J48.
- [x] Asociar reglas a una prediccion concreta.
- [x] Limitar salida a reglas legibles para el frontend.

## Fase 6: Implementar `/explain`

- [x] Definir contrato de entrada para explicacion.
- [x] Integrar cliente de Claude.
- [x] Construir prompt con reglas y contexto Titanic.
- [x] Devolver narrativa historica breve.

## Fase 7: Mini simulador what-if

- [x] Permitir cambiar una variable del perfil.
- [x] Recalcular la probabilidad.
- [x] Mostrar diferencia entre escenario original y modificado.

## Fase 8: Validacion y cierre

- [x] Verificar que el proyecto compila tras los cambios hechos.
- [ ] Verificar el flujo completo de upload y preview.
- [ ] Verificar el flujo de prediccion.
- [ ] Verificar el flujo de explicacion.
- [ ] Verificar tiempo total del MVP.

## Resumen corto

- Hecho: contrato inicial, lectura de dataset, upload, preview, validacion, normalizacion, entrenamiento J48, prediccion, extraccion de reglas, explicacion y simulador what-if.
- En curso: validacion final del flujo completo.
- Pendiente principal: cierre del flujo completo y pruebas funcionales.