// MazeSolver.kt
package ve.usb.libGrafo

data class MazeSolution(
    val isPossible: Boolean,
    val optimalPath: List<Position>? = null,
    val steps: Int = 0,
    val finalHealth: Int = 0,
    val allOptimalPaths: List<List<Position>> = emptyList()
)

class MazeSolver(private val maze: Maze) {
    
    fun solve(): MazeSolution {
        // Primero verificamos conectividad básica
        if (!hasBasicConnectivity()) {
            return MazeSolution(isPossible = false)
        }
        
        // Buscamos todas las rutas factibles
        val feasiblePaths = findFeasiblePaths()
        
        if (feasiblePaths.isEmpty()) {
            return MazeSolution(isPossible = false)
        }
        
        // Encontramos las rutas óptimas (menor número de pasos)
        val minSteps = feasiblePaths.minOf { it.steps }
        val optimalPaths = feasiblePaths.filter { it.steps == minSteps }
        
        // Tomamos la primera ruta óptima para los resultados principales
        val firstOptimal = optimalPaths.first()
        
        return MazeSolution(
            isPossible = true,
            optimalPath = firstOptimal.path,
            steps = minSteps,
            finalHealth = firstOptimal.health, // Usamos health en lugar de finalHealth
            allOptimalPaths = optimalPaths.map { it.path }
        )
    }
    
    private fun hasBasicConnectivity(): Boolean {
        // BFS simple ignorando vida y tesoros
        val visited = Array(maze.rows) { BooleanArray(maze.cols) }
        val queue = ArrayDeque<Position>()
        val start = maze.getStart()
        
        queue.add(start)
        visited[start.x][start.y] = true
        
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            
            if (current == maze.getEnd()) {
                return true
            }
            
            for (neighbor in maze.getNeighbors(current)) {
                if (!visited[neighbor.x][neighbor.y]) {
                    visited[neighbor.x][neighbor.y] = true
                    queue.add(neighbor)
                }
            }
        }
        
        return false
    }
    
    private data class PathState(
        val position: Position,
        val health: Int,
        val collectedTreasures: Set<Position>,
        val path: List<Position>,
        val steps: Int
    )
    
    private fun findFeasiblePaths(): List<PathState> {
        val start = maze.getStart()
        val end = maze.getEnd()
        val feasiblePaths = mutableListOf<PathState>()
        
        // Usamos BFS con estado extendido
        val visited = mutableSetOf<StateKey>()
        val queue = ArrayDeque<PathState>()
        
        val initialState = PathState(
            position = start,
            health = maze.initialHealth,
            collectedTreasures = emptySet(),
            path = listOf(start),
            steps = 0
        )
        
        queue.add(initialState)
        visited.add(StateKey(start, maze.initialHealth, emptySet()))
        
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            
            if (current.position == end) {
                if (current.health >= 1) {
                    feasiblePaths.add(current)
                }
                continue
            }
            
            for (neighbor in maze.getNeighbors(current.position)) {
                // Determinar si estamos recogiendo un tesoro
                val isTreasure = maze.grid[neighbor.x][neighbor.y] == 'T'
                val newCollectedTreasures = if (isTreasure && neighbor !in current.collectedTreasures) {
                    current.collectedTreasures + neighbor
                } else {
                    current.collectedTreasures
                }
                
                // Calcular el costo/cambio de vida
                val cost = when {
                    neighbor == end -> 0
                    neighbor == start -> 0
                    isTreasure && neighbor in newCollectedTreasures -> -5 // +5 vida
                    maze.grid[neighbor.x][neighbor.y] in '0'..'9' -> 
                        maze.grid[neighbor.x][neighbor.y].toString().toInt()
                    else -> 0
                }
                
                val newHealth = current.health - cost
                
                // Verificar si el corredor sobrevive (solo importa si no es el destino final)
                if (newHealth <= 0 && neighbor != end) {
                    continue
                }
                
                val newState = PathState(
                    position = neighbor,
                    health = newHealth,
                    collectedTreasures = newCollectedTreasures,
                    path = current.path + neighbor,
                    steps = current.steps + 1
                )
                
                val stateKey = StateKey(neighbor, newHealth, newCollectedTreasures)
                
                // Solo explorar si no hemos visitado este estado
                if (stateKey !in visited) {
                    visited.add(stateKey)
                    queue.add(newState)
                }
            }
        }
        
        return feasiblePaths
    }
    
    private data class StateKey(
        val position: Position,
        val health: Int,
        val collectedTreasures: Set<Position>
    )
}