// Main.kt
import ve.usb.libGrafo.*

fun main() {
    try {
        // Crear archivo de prueba si no existe
        val testMaze = """
            N=10, M=10, P=15
            ##########
            #S0000000#
            #0#######0#
            #0#T000#0#
            #0#####0#0#
            #0#####0#0#
            #0#00000#0#
            #0#######0#
            #00000000E#
            ##########
        """.trimIndent()
        
        java.io.File("laberinto.txt").writeText(testMaze)
        
        // Probar el parser y solver
        val maze = MazeParser.parseFromFile("laberinto.txt")
        val solver = MazeSolver(maze)
        val solution = solver.solve()
        
        println("=== SOLUCIÓN DEL LABERINTO ===")
        println("Es posible: ${solution.isPossible}")
        
        if (solution.isPossible) {
            println("Pasos óptimos: ${solution.steps}")
            println("Vida final: ${solution.finalHealth}")
            println("Ruta óptima:")
            solution.optimalPath?.forEachIndexed { index, pos ->
                println("  Paso $index: $pos")
            }
        } else {
            println("No hay ruta factible desde S hasta E")
        }
        
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    }
}