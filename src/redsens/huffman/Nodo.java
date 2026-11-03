class Nodo
{
    Character caracter; // simbolo contenido en el nodo
    Integer frecuencia;  // frecuencia del simbolo 
    Nodo izq = null, der = null; // punteros


    // constructores del nodo

    Nodo(Character caracter, Integer frecuencia)
    {
        this.caracter = caracter;
        this.frecuencia = frecuencia;

    }

    public Nodo(Character caracter, Integer frecuencia, Nodo izq, Nodo der)
    {
        this.caracter = caracter;
        this.frecuencia = frecuencia;
        this.izq = izq;
        this.der = der;
    }

}