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
-   Integrante 2 - Francisco Luna : Herramienta utilizada: Claude. La usé para organizar el trabajo en grupo, para que me explicara paso a paso los conceptos del M2 (ABB, AVL, recorridos y rotaciones), para generar el código base del módulo por etapas junto con sus pruebas y para preparar mi guía de exposición. Un error de la IA que detecté: propuso ordenar el índice por el código de la estación (String), pero el enunciado define como clave del M2 el número de estación (entero). Lo corregí cambiando la interfaz y los árboles para que usen numero. Todo lo que usé lo verifiqué ejecutándolo: 49 pruebas propias con resultados calculados a mano y el experimento del M2, cuyas alturas dan siempre lo mismo con la semilla del grupo.
-   Integrante 3 : [completar rol]
-   Integrante 4  : Matilda Ferraris: Herramienta utilizada: Claude. Usé la IA en cuatro momentos del trabajo. Primero, para organizar como debería implementar el paquete que contiene todo lo necesario para la compresión y que debía importar. Segundo, para resolver problemas de recursión accidentales en el código. Tercero, para implementar la clase CompresorArchivo, que genera un archivo .huf las frecuencias para luego la reconstrucción del arbol. Por último, para revisar por posibles errores de lógica o indentación. Un error de la IA durante el proceso: la función decodificar tenía un return de más que repetía y saltaba caracteres y terminaba en excepción.
-   Integrante 5 - Luis Defago : Herramienta utilizada: Claude. Usé la IA en tres momentos del trabajo. Primero, para implementar el M5 (solución simple con LinkedList e iterador, alternativa con ABB de k-ésimo, calendario, experimento y pruebas), pidiéndole que me fuera explicando lo que hacía y por qué elegía cada forma de resolverlo. Segundo, para integrar las ramas de todos en una rama del M6, resolver los conflictos del menú y conectar los módulos entre sí (carga de la red en la tabla hash y en el AVL, borrado sincronizado entre M1 y M2 y batería de experimentos). Por último, para hacer una prueba general de todos los módulos y regenerar el informe de rendimiento. Errores de la IA que detecté: al planificar el M5 asumió que M=1 elimina a la persona actual, pero al revisar el enunciado y la lectura de Weiss vimos que M es la cantidad de pasadas (con N=5 y M=1 salen 2, 4, 1, 5 y queda el 3), y se corrigió antes de programar. Otro: al sincronizar el borrado entre M1 y M2 introdujo un error en el listado por rango del M2, y la prueba con la que mostró la corrección también pasaba con el código viejo; se lo marqué y la rehicimos con un caso que sí lo demostraba.
