# RedSens - TP Integral AED2 (Grupo 4)

## Integrantes y roles

-   Valentino Rossi  : M1 (registro de estaciones con tabla hash) y la parte de M6 que sostiene a M1 (generación de datos con semilla, verificación de aleatorios e informe de rendimiento).
-   Francisco Luna   : M2 (índice ordenado con ABB/AVL: listado en orden, consultas por rangos y detección de críticas) y la parte de M6 que sostiene a M2 (pruebas con datos generados y medición de rendimiento).
-   Integrante 3  : M3 (red vial con grafos: matriz y lista de adyacencia, BFS, DFS con corte de rutas, Dijkstra, Floyd y plan de visitas de la cuadrilla con el vecino más cercano).
-   Matilda Ferraris  : M4 (Implementacion del Algoritmo de Huffman)
-   Luis Defago  : M5 (guardias rotativas con Josephus: solución simple con lista enlazada e iterador, alternativa eficiente con ABB de k-ésimo y calendario de guardias) y M6 (integración de los seis módulos en el menú, batería de experimentos e informe de rendimiento).

## Instrucciones para compilar y ejecutar

Requisitos: JDK 17 o superior.

Ejecutar todos los comandos desde la carpeta `RedSens` (la que contiene `src` y `datos`).

Compilar:

```
javac -encoding UTF-8 -d out $(find src -name '*.java')
```

En Windows PowerShell:

```
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
```

Ejecutar el menú principal:

```
java -cp out redsens.app.Main
```

Ejecutar las pruebas de cada módulo:

```
java -cp out redsens.app.VerificacionM1
java -cp out redsens.app.VerificacionM2
java -cp out redsens.app.VerificacionM3
java -cp out redsens.app.VerificacionM4
java -cp out redsens.app.VerificacionM5
java -cp out redsens.app.VerificacionM6
```

La red vial del M3 se carga desde `datos/rutas.txt` (una ruta por línea: `A;B;km`).

Si no se indica el número de grupo, se usa el grupo 4.

En Visual Studio Code: abrir la carpeta `RedSens`, ir a Run and Debug y elegir `RedSens (menú)`.

## Menú principal

| Opción | Módulo | Qué permite |
|---|---|---|
| 1 | M1 · Registro | Cargar la red (N estaciones en la tabla hash **y** en el AVL), buscar y eliminar por código, estado y experimento |
| 2 | M2 · Índice ordenado | Listar por rango de números, reporte de críticas, estado del AVL, recorridos y experimento ABB vs AVL |
| 3 | M3 · Red vial | Representación del grafo, BFS, DFS con corte de rutas, Dijkstra, Floyd y plan de visitas a partir de las críticas del M2 |
| 4 | M4 · Transmisión | Caso WWXWYZWX y generar, comprimir, descomprimir y verificar el archivo de mediciones |
| 5 | M5 · Guardias | Calendario de guardias (N = 12, M del grupo) y experimento lista vs. árbol |
| 6 | M6 · Simulación | Datos con semilla, semillas iguales/distintas, uniformidad, archivo de mediciones y **batería de experimentos** (M1, M2 y M5) |
| 7 | Informe | Informe de rendimiento acumulado (también se guarda en `datos/informe_rendimiento.txt`) |

Primero hay que cargar la red (opción 1 → 1): M2 y el plan de visitas de M3 trabajan sobre esas estaciones. Eliminar una estación en M1 también la saca del índice de M2.

## Bitácora de IA
-   Integrante 1 - Valentino Rossi : Herramienta utilizada: Claude.  Usé la IA en tres momentos del trabajo. Primero, para dividir los módulos del trabajo (M1 a M6) entre los integrantes de manera equitativa. Segundo, para que me diera ideas de cómo abordar el tema y encarar cada problema antes de programarlo. Por último, una vez escrito el código, la usé para revisar que no hubiera errores de lógica.
-   Integrante 2 : - Integrante 2 - Francisco Luna : Herramienta utilizada: Claude. Usé la IA en cuatro momentos del trabajo. Primero, para organizar el trabajo en grupo: cómo repartir los módulos y cómo usar Git. Segundo, para que me explicara paso a paso los conceptos de mi módulo (ABB, AVL, recorridos y rotaciones) antes de programarlos. Tercero, para generar el código base del M2 por pasos y las pruebas con resultados calculados a mano, que después ejecuté y verifiqué. Por último, para armar mi guía de estudio y de exposición. Un error de la IA que detecté: propuso ordenar el índice por el código de la estación (String), pero al revisar el enunciado completo se vio que la clave del M2 es el número de estación, y se corrigió. Otro: anticipó que el AVL insertaba más lento con datos aleatorios, pero mi tabla de resultados no lo sostenía, así que se eliminó esa conclusión.
-   Integrante 3 : [completar rol]
-   Integrante 4  : Matilda Ferraris: Herramienta utilizada: Claude. Usé la IA en cuatro momentos del trabajo. Primero, para organizar como debería implementar el paquete que contiene todo lo necesario para la compresión y que debía importar. Segundo, para resolver problemas de recursión accidentales en el código. Tercero, para implementar la clase CompresorArchivo, que genera un archivo .huf las frecuencias para luego la reconstrucción del arbol. Por último, para revisar por posibles errores de lógica o indentación. Un error de la IA durante el proceso: la función decodificar tenía un return de más que repetía y saltaba caracteres y terminaba en excepción.
-   Integrante 5  : [completar rol]
