package ve.usb.libGrafo

interface Grafo : Iterable<Lado> {
    fun numeroDeLados(): Int // Retorna el número de lados del grafo
    fun numeroDeVertices(): Int // Retorna el número de vértices del grafo
    fun adyacentes(v: Int): Iterable<Lado> // Retorna los adyacentes de v
    override operator fun iterator(): Iterator<Lado> // Retorna un iterador de los lados del grafo
    // Laboratorio semana 3
    fun gradoEntrada(v: Int): Int
    fun gradoSalida(v: Int): Int
    fun gradoTotal(v: Int): Int
    fun gradoMaximo(): Int
    fun gradoMinimo(): Int
    fun gradoMedio(): Double
    fun numeroVerticesAislados(): Int
    // Laboratorio semana 4
    fun componentesConexas(): List<List<Int>>
    // Laboratorio semana 5
    fun aBFS(inicio: Int) : List<Int>
    fun componentesConexasBFS(): List<List<Int>>
    // Laboratorio semana 6
    fun aDFS(inicio: Int) : List<Int>
    fun componentesConexasDFS(): List<List<Int>>
}