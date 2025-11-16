// Maze.kt
package ve.usb.libGrafo

data class Position(val x: Int, val y: Int) {
    fun move(dx: Int, dy: Int): Position = Position(x + dx, y + dy)
    fun isValid(rows: Int, cols: Int): Boolean = 
        x in 0 until rows && y in 0 until cols
        
    override fun toString(): String = "($x,$y)"
}

class Maze(
    val rows: Int,
    val cols: Int,
    val initialHealth: Int,
    val grid: List<List<Char>>
) {
    private val start: Position = findUniqueCell('S')
    private val end: Position = findUniqueCell('E')
    
    // Grafo para representar la conectividad del laberinto
    private val grafo: GrafoNoDirigido = GrafoNoDirigido(rows * cols)
    
    init {
        validateMaze()
        buildGraph()
    }
    
    private fun findUniqueCell(char: Char): Position {
        val positions = mutableListOf<Position>()
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                if (grid[i][j] == char) {
                    positions.add(Position(i, j))
                }
            }
        }
        require(positions.size == 1) { "Debe haber exactamente una celda $char" }
        return positions.first()
    }
    
    private fun validateMaze() {
        require(rows >= 10 && cols >= 10) { "El laberinto debe ser al menos 10x10" }
        require(initialHealth >= 10) { "La vida inicial debe ser al menos 10" }
    }
    
    private fun buildGraph() {
        // Construir el grafo de conectividad
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val currentPos = Position(i, j)
                if (!isWall(currentPos)) {
                    val currentVertex = positionToVertex(currentPos)
                    
                    // Conectar con vecinos válidos
                    getNeighbors(currentPos).forEach { neighbor ->
                        val neighborVertex = positionToVertex(neighbor)
                        if (!grafo.tieneArista(currentVertex, neighborVertex)) {
                            grafo.agregarArista(currentVertex, neighborVertex)
                        }
                    }
                }
            }
        }
    }
    
    private fun positionToVertex(position: Position): Int = position.x * cols + position.y
    private fun vertexToPosition(vertex: Int): Position = Position(vertex / cols, vertex % cols)
    
    fun getStart(): Position = start
    fun getEnd(): Position = end
    
    fun isWall(position: Position): Boolean = 
        grid[position.x][position.y] == '#'
    
    fun isValidMove(position: Position): Boolean = 
        position.isValid(rows, cols) && !isWall(position)
    
    fun getNeighbors(position: Position): List<Position> {
        val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        return directions.map { (dx, dy) -> position.move(dx, dy) }
            .filter { isValidMove(it) }
    }
    
    // Usar BFS de libGrafo para verificar conectividad
    fun isConnectedUsingLibGrafo(): Boolean {
        val startVertex = positionToVertex(start)
        val endVertex = positionToVertex(end)
        
        // Usar BFS de libGrafo para encontrar si hay camino
        val visited = grafo.aBFS(startVertex)
        return endVertex in visited
    }
    
    // Obtener camino usando componentes conexas de libGrafo
    fun getPathUsingLibGrafo(): List<Position>? {
        val startVertex = positionToVertex(start)
        val endVertex = positionToVertex(end)
        
        // Encontrar la componente conexa que contiene el start
        val componentes = grafo.componentesConexasBFS()
        val componenteStart = componentes.find { it.contains(startVertex) }
        
        if (componenteStart != null && componenteStart.contains(endVertex)) {
            // Reconstruir camino usando BFS desde start
            return reconstructPathBFS(startVertex, endVertex).map { vertexToPosition(it) }
        }
        
        return null
    }
    
    private fun reconstructPathBFS(start: Int, end: Int): List<Int> {
        val visited = BooleanArray(rows * cols)
        val parent = IntArray(rows * cols) { -1 }
        val queue = ArrayDeque<Int>()
        
        queue.add(start)
        visited[start] = true
        parent[start] = start
        
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            
            if (current == end) break
            
            grafo.adyacentes(current).forEach { lado ->
                val neighbor = lado.vecino(current)
                if (!visited[neighbor]) {
                    visited[neighbor] = true
                    parent[neighbor] = current
                    queue.add(neighbor)
                }
            }
        }
        
        // Reconstruir camino
        val path = mutableListOf<Int>()
        var current = end
        while (current != start) {
            path.add(current)
            current = parent[current]
            if (current == -1) return emptyList() // No hay camino
        }
        path.add(start)
        
        return path.reversed()
    }
    
    fun printMaze() {
        println("Laberinto: $rows x $cols")
        println("Start: $start, End: $end")
        println("Vida inicial: $initialHealth")
        println("Vértices en grafo: ${grafo.numeroDeVertices()}")
        println("Aristas en grafo: ${grafo.numeroDeLados()}")
        println("\nGrid:")
        grid.forEach { row ->
            println(row.joinToString(""))
        }
    }
}