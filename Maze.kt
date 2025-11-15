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
    
    init {
        validateMaze()
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
}