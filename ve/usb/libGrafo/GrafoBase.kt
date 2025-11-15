package ve.usb.libGrafo

abstract class GrafoBase(
    protected val dirigido: Boolean,
    protected val conCosto: Boolean
) : Grafo {
    protected var v = 0
    protected var e = 0
    protected val listaAdy = mutableMapOf<Int, MutableList<Lado>>()

    constructor(fuente: String, dirigido: Boolean, conCosto: Boolean) : this(dirigido, conCosto) {
        leerDesdeFuente(fuente)
    }

    override fun numeroDeVertices() = v
    override fun numeroDeLados() = e
    override fun adyacentes(v: Int): Iterable<Lado> {
        validarVertice(v)
        return listaAdy[v] ?: emptyList()
    }

    protected open fun agregarLado(lado: Lado) {
        listaAdy.computeIfAbsent(lado.u) { mutableListOf() }.add(lado)

        if (!dirigido && lado is Arista) {
            val inversa = when (lado) {
                is AristaCosto -> AristaCosto(lado.v, lado.u, lado.costo())
                else -> Arista(lado.v, lado.u)
            }
            listaAdy.computeIfAbsent(lado.v) { mutableListOf() }.add(inversa)
        }
        e++
    }

    override fun iterator(): Iterator<Lado> {
        if (!dirigido) {
            // En grafo no dirigido, cada arista aparece 2 veces
            // Evitar duplicados guardando pares normalizados (min, max)
            val ladosUnicos = mutableSetOf<Pair<Int, Int>>()
            val lados = mutableListOf<Lado>()
            for ((_, vecinos) in listaAdy) {
                for (lado in vecinos) {
                    // Normalizar: el menor vértice siempre primero
                    val par = if (lado.u < lado.v) {
                        Pair(lado.u, lado.v)
                    } else {
                        Pair(lado.v, lado.u)
                    }
                    // Solo agregar si no lo hemos visto antes
                    if (par !in ladosUnicos) {
                        ladosUnicos.add(par)
                        lados.add(lado)
                    }
                }
            }
            return lados.iterator()
        } else {
            // En grafo dirigido, no hay duplicados
            return listaAdy.values.flatten().iterator()
        }
    }

    protected fun validarVertice(vertice: Int) {
        require(vertice in 0 until v) {
            "Vértice $vertice fuera de rango [0, ${v - 1}]"
        }
    }

    protected fun leerDesdeFuente(fuente: String) {
        val texto = if (java.io.File(fuente).exists()) {
            java.io.File(fuente).readText()
        } else {
            fuente // interpreta directamente el string
        }

        // Filtrar líneas con # (comentarios)
        val textoSinComentarios = texto.lines()
            .map {
                val lineaSinComentario = it.substringBefore("#").trim()
                lineaSinComentario
            }
            .filter { it.isNotBlank() }
            .filterNot { it.startsWith("tipo=") }
            .joinToString(" ")

        val tokens = textoSinComentarios.split("[;\\s\\n]+".toRegex()).filter { it.isNotBlank() }

        if (tokens.isEmpty()) return

        // El primer token es el número de vértices
        v = tokens[0].toInt()
        require(v >= 0) { "El número de vértices debe ser no negativo: $v" }

        // Inicializar listas de adyacencia para todos los vértices
        for (i in 0 until v) {
            listaAdy[i] = mutableListOf()
        }

        // Los tokens restantes son los lados
        for (i in 1 until tokens.size) {
            val t = tokens[i]
            val datos = t.removePrefix("(").removeSuffix(")").split(",")

            val u = datos[0].trim().toInt()
            val v = datos[1].trim().toInt()
            val w = datos.getOrNull(2)?.trim()?.toDoubleOrNull()

            // Validar que los vértices estén en el rango válido [0, this.v)
            require(u in 0 until this.v) {
                "Vértice fuera de rango: $u (debe estar en [0, ${this.v}))"
            }
            require(v in 0 until this.v) {
                "Vértice fuera de rango: $v (debe estar en [0, ${this.v}))"
            }

            val lado = when {
                dirigido && conCosto -> ArcoCosto(u, v, w ?: 0.0)
                dirigido -> Arco(u, v)
                conCosto -> AristaCosto(u, v, w ?: 0.0)
                else -> Arista(u, v)
            }
            agregarLado(lado)
        }
    }

    override fun toString(): String = buildString {
        val tipo = this@GrafoBase::class.simpleName ?: "Grafo"
        val v = this@GrafoBase.numeroDeVertices()
        val e = this@GrafoBase.numeroDeLados()

        appendLine("tipo=$tipo v=$v e=$e")
        if (e > 0) {
            appendLine("Lados:")
            val lados = mutableListOf<String>()
            this@GrafoBase.forEach { lados.add(it.toString()) }
            // Formatear con saltos de línea cada 10 lados para legibilidad
            lados.chunked(10).forEach { chunk ->
                appendLine("  ${chunk.joinToString(" ")}")
            }
        }
    }

    override fun gradoEntrada(v: Int): Int {
        validarVertice(v)
        return if (!dirigido) {
            listaAdy[v]?.size ?: 0
        } else {
            listaAdy.values.flatten().count { it.v == v }
        }
    }

    override fun gradoSalida(v: Int): Int {
        validarVertice(v)
        return listaAdy[v]?.size ?: 0
    }

    override fun gradoTotal(v: Int): Int {
        validarVertice(v)
        return gradoEntrada(v) + gradoSalida(v)
    }

    override fun gradoMaximo(): Int {
        if (v == 0) return 0
        return (0 until v).maxOf { gradoTotal(it) }
    }

    override fun gradoMinimo(): Int {
        if (v == 0) return 0
        return (0 until v).minOf { gradoTotal(it) }
    }

    override fun gradoMedio(): Double {
        if (v == 0) return 0.0
        val sumaGrados = (0 until v).sumOf { gradoTotal(it) }
        return sumaGrados.toDouble() / v
    }

    override fun numeroVerticesAislados(): Int {
        return (0 until v).count { vertice ->
            gradoEntrada(vertice) == 0 && gradoSalida(vertice) == 0
        }
    }

    override fun componentesConexas(): List<List<Int>> {
        val n = numeroDeVertices()
        val alcanzable = Array(n) { BooleanArray(n) }
        for (v in 0 until n) {
            alcanzable[v][v] = true
            for (lado in adyacentes(v)) {
                val u = lado.vecino(v)
                alcanzable[v][u] = true
                if (!dirigido) alcanzable[u][v] = true
            }
        }
        // Clausura transitiva de Warshall
        for (k in 0 until n)
            for (i in 0 until n)
                if (alcanzable[i][k]) // evita revisar k inútiles
                    for (j in 0 until n)
                        alcanzable[i][j] = alcanzable[i][j] || (alcanzable[k][j])
        // Extraer componentes
        val visitado = BooleanArray(n) { false }
        val componentes = mutableListOf<List<Int>>()
        for (v in 0 until n) {
            if (!visitado[v]) {
                val componente = mutableListOf<Int>()
                for (u in 0 until n) {
                    val enMismaComponente = if (dirigido) {
                        alcanzable[v][u] && alcanzable[u][v]  // Fuertemente conexo
                    } else {
                        alcanzable[v][u]  // Simplemente conexo
                    }
                    if (enMismaComponente && !visitado[u]) {
                        componente.add(u)
                        visitado[u] = true
                    }
                }
                componentes.add(componente)
            }
        }
        return componentes
    }

    override fun aBFS(inicio: Int): List<Int> {
        validarVertice(inicio)
        val visitado = BooleanArray(numeroDeVertices())
        val cola = ArrayDeque<Int>().apply { add(inicio) }
        val componente = mutableListOf(inicio)
        visitado[inicio] = true
        while (cola.isNotEmpty()) {
            val v = cola.removeFirst()
            for (lado in adyacentes(v)) {
                val u = lado.vecino(v)
                if (!visitado[u]) {
                    visitado[u] = true
                    componente.add(u)
                    cola.add(u)
                }
            }
        }
        return componente
    }

    override fun componentesConexasBFS(): List<List<Int>> {
        val n = numeroDeVertices()
        val visitado = BooleanArray(n)
        val componentes = mutableListOf<List<Int>>()
        (0 until n).forEach { s ->
            if (!visitado[s]) {
                val componente = aBFS(s)
                componente.forEach { visitado[it] = true }
                componentes.add(componente)
            }
        }
        return componentes
    }

    override fun aDFS(inicio: Int): List<Int> {
        validarVertice(inicio)
        val visitado = BooleanArray(numeroDeVertices())
        val pila = ArrayDeque<Int>().apply { add(inicio) }
        val componente = mutableListOf<Int>()
        while (pila.isNotEmpty()) {
            val v = pila.removeLast() // removeLast() para comportamiento LIFO (pila)
            if (!visitado[v]) {
                visitado[v] = true
                componente.add(v)
                for (lado in adyacentes(v)) {
                    val u = lado.vecino(v)
                    if (!visitado[u]) {
                        pila.add(u)
                    }
                }
            }
        }
        return componente
    }

    override fun componentesConexasDFS(): List<List<Int>> {
        val n = numeroDeVertices()
        val visitado = BooleanArray(n)
        val componentes = mutableListOf<List<Int>>()
        (0 until n).forEach { s ->
            if (!visitado[s]) {
                val componente = aDFS(s)
                componente.forEach { visitado[it] = true }
                componentes.add(componente)
            }
        }
        return componentes
    }
}

// Clases intermedias
abstract class GrafoNoDirigidoBase(conCosto: Boolean) : GrafoBase(dirigido = false, conCosto = conCosto) {
    constructor(fuente: String, conCosto: Boolean) : this(conCosto) {
        leerDesdeFuente(fuente)
    }

    fun tieneArista(v1: Int, v2: Int): Boolean {
        validarVertice(v1)
        validarVertice(v2)
        return listaAdy[v1]?.any { it.v == v2 } ?: false
    }
}

abstract class GrafoDirigidoBase(conCosto: Boolean) : GrafoBase(dirigido = true, conCosto = conCosto) {
    constructor(fuente: String, conCosto: Boolean) : this(conCosto) {
        leerDesdeFuente(fuente)
    }

    fun tieneArco(v1: Int, v2: Int): Boolean {
        validarVertice(v1)
        validarVertice(v2)
        return listaAdy[v1]?.any { it.v == v2 } ?: false
    }
}