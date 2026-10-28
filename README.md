# RedSens - TP Integral AED2 (Grupo 4)

## Integrantes y roles

-   Valentino Rossi  : M1 (registro de estaciones con tabla hash) y la parte de M6 que sostiene a M1 (generación de datos con semilla, verificación de aleatorios e informe de rendimiento).
-   Francisco Luna   : M2 (índice ordenado con ABB/AVL: listado en orden, consultas por rangos y detección de críticas) y la parte de M6 que sostiene a M2 (pruebas con datos generados y medición de rendimiento).
-   Integrante 3  : [completar rol]
-   Integrante 4  : [completar rol]
-   Integrante 5  : [completar rol]

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
java -cp out redsens.app.VerificacionM6
```

Si no se indica el número de grupo, se usa el grupo 4.

En Visual Studio Code: abrir la carpeta `RedSens`, ir a Run and Debug y elegir `RedSens (menú)`.

## Bitácora de IA
-   Integrante 1 - Valentino Rossi : Herramienta utilizada: Claude.  Usé la IA en tres momentos del trabajo. Primero, para dividir los módulos del trabajo (M1 a M6) entre los integrantes de manera equitativa. Segundo, para que me diera ideas de cómo abordar el tema y encarar cada problema antes de programarlo. Por último, una vez escrito el código, la usé para revisar que no hubiera errores de lógica.
-   Integrante 2 : [completar rol]
-   Integrante 3 : [completar rol]
-   Integrante 4  : [completar rol]
-   Integrante 5  : [completar rol]

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
java -cp out redsens.app.VerificacionM6
```

Si no se indica el número de grupo, se usa el grupo 4.

En Visual Studio Code: abrir la carpeta `RedSens`, ir a Run and Debug y elegir `RedSens (menú)`.
