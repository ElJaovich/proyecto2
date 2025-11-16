// MazeSolver.kt
package ve.usb.libGrafo

data class MazeSolution(
    val isPossible: Boolean,
    val optimalPath: List<Position>? = null,
    val steps: Int = 0,
    val message: String = "",
    val graphStats: GraphStats? = null
)

data class GraphStats(
    val vertices: Int,
    val edges: Int,
    val connectedComponents: Int,
    val startComponentSize: Int,
    val endComponentSize: Int
)

class MazeSolver(private val maze: Maze) {
    
    fun solveBasicConnectivity(): MazeSolution {
        // 1. Verificar conectividad usando libGrafo
        val isConnected = maze.isConnectedUsingLibGrafo()
        val path = maze.getPathUsingLibGrafo()
        
        // 2. Obtener estadísticas del grafo
        val stats = getGraphStatistics()
        
        if (!isConnected) {
            return MazeSolution(
                isPossible = false,
                message = "E es inalcanzable desde S por paredes (problema estructural)",
                graphStats = stats
            )
        }
        
        return MazeSolution(
            isPossible = true,
            optimalPath = path,
            steps = path?.size?.minus(1) ?: 0,
            message = "Conectividad básica verificada - E es alcanzable desde S",
            graphStats = stats
        )
    }
    
    /**
     * Verificación rápida usando libGrafo
     */
    fun isEndReachable(): Boolean = maze.isConnectedUsingLibGrafo()
    
    /**
     * Análisis completo de conectividad usando libGrafo
     */
    fun analyzeConnectivity(): ConnectivityAnalysis {
        val grafo = maze.getGraphForAnalysis()
        val componentes = grafo.componentesConexasBFS()
        val startVertex = maze.positionToVertex(maze.getStart())
        val endVertex = maze.positionToVertex(maze.getEnd())
        
        var startComponent: Set<Position>? = null
        var endComponent: Set<Position>? = null
        
        componentes.forEach { componente ->
            val posiciones = componente.map { maze.vertexToPosition(it) }.toSet()
            if (startVertex in componente) startComponent = posiciones
            if (endVertex in componente) endComponent = posiciones
        }
        
        return ConnectivityAnalysis(
            components = componentes.map { comp -> comp.map { maze.vertexToPosition(it) }.toSet() },
            startComponent = startComponent,
            endComponent = endComponent,
            isConnected = startComponent == endComponent && startComponent != null,
            graphStats = getGraphStatistics()
        )
    }
    
    private fun getGraphStatistics(): GraphStats {
        val grafo = maze.getGraphForAnalysis()
        val componentes = grafo.componentesConexasBFS()
        val startVertex = maze.positionToVertex(maze.getStart())
        val endVertex = maze.positionToVertex(maze.getEnd())
        
        val startComponentSize = componentes.find { it.contains(startVertex) }?.size ?: 0
        val endComponentSize = componentes.find { it.contains(endVertex) }?.size ?: 0
        
        return GraphStats(
            vertices = grafo.numeroDeVertices(),
            edges = grafo.numeroDeLados(),
            connectedComponents = componentes.size,
            startComponentSize = startComponentSize,
            endComponentSize = endComponentSize
        )
    }
}

// Extender Maze para proporcionar acceso al grafo
private fun Maze.getGraphForAnalysis(): GrafoNoDirigido {
    // Crear un grafo temporal para análisis
    val grafo = GrafoNoDirigido(rows * cols)
    
    for (i in 0 until rows) {
        for (j in 0 until cols) {
            val currentPos = Position(i, j)
            if (!isWall(currentPos)) {
                val currentVertex = positionToVertex(currentPos)
                getNeighbors(currentPos).forEach { neighbor ->
                    val neighborVertex = positionToVertex(neighbor)
                    if (!grafo.tieneArista(currentVertex, neighborVertex)) {
                        grafo.agregarArista(currentVertex, neighborVertex)
                    }
                }
            }
        }
    }
    
    return grafo
}

private fun Maze.positionToVertex(position: Position): Int = position.x * cols + position.y
private fun Maze.vertexToPosition(vertex: Int): Position = Position(vertex / cols, vertex % cols)

data class ConnectivityAnalysis(
    val components: List<Set<Position>>,
    val startComponent: Set<Position>?,
    val endComponent: Set<Position>?,
    val isConnected: Boolean,
    val graphStats: GraphStats
) {
    val componentCount: Int get() = components.size
}