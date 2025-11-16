// MazeParser.kt
package ve.usb.libGrafo

import java.io.File

class MazeParser {
    companion object {
        fun parseFromFile(filename: String): Maze {
            val lines = File(filename).readLines().map { it.trim() }
            require(lines.isNotEmpty()) { "Archivo vacío" }
            
            // Parsear primera línea: "N=20, M=20, P=15"
            val firstLine = lines[0]
            val params = parseParameters(firstLine)
            
            val rows = params["N"] ?: throw IllegalArgumentException("Falta parámetro N")
            val cols = params["M"] ?: throw IllegalArgumentException("Falta parámetro M") 
            val health = params["P"] ?: throw IllegalArgumentException("Falta parámetro P")
            
            // Parsear grid - tomar las siguientes 'rows' líneas
            val gridLines = lines.subList(1, 1 + rows)
            
            // Validar y normalizar las líneas del grid - ESPECIFICAR TIPO List<List<Char>>
            val grid: List<List<Char>> = gridLines.map { line ->
                val normalizedLine = if (line.length < cols) {
                    line.padEnd(cols, ' ')
                } else if (line.length > cols) {
                    line.substring(0, cols)
                } else {
                    line
                }
                normalizedLine.toList()
            }
            
            return Maze(rows, cols, health, grid)
        }
        
        private fun parseParameters(line: String): Map<String, Int> {
            return line.split(",")
                .associate { param ->
                    val parts = param.trim().split("=")
                    require(parts.size == 2) { "Formato de parámetro inválido: $param" }
                    parts[0].trim() to parts[1].trim().toInt()
                }
        }
    }
}