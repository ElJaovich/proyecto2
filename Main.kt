// Main.kt
import ve.usb.libGrafo.*
import java.io.File

fun main(args: Array<String>) {
    println("=== SEMANA 9 - VERIFICADOR DE LABERINTO MAZE ===\n")
    
    // Verificar que se proporcionó un archivo
    if (args.isEmpty()) {
        println("❌ Uso: java -jar test.jar <archivo_laberinto.txt>")
        println("   Ejemplo: java -jar test.jar laberinto.txt")
        return
    }
    
    val filename = args[0]
    val file = File(filename)
    
    if (!file.exists()) {
        println("❌ El archivo '$filename' no existe")
        return
    }
    
    // Verificar el laberinto
    verifyMazeFile(filename)
}

fun verifyMazeFile(filename: String) {
    println("🔍 VERIFICANDO LABERINTO: $filename")
    println("=".repeat(50))
    
    try {
        // 1. Lectura e interpretación del mapa desde archivo TXT
        println("\n1. 📖 LECTURA E INTERPRETACION DEL MAPA")
        println("-".repeat(40))
        val maze = MazeParser.parseFromFile(filename)
        maze.printMaze()
        
        // 2. Verificación de validez del laberinto
        println("\n2. ✅ VERIFICACION DE VALIDEZ")
        println("-".repeat(40))
        println("   • S unico: ✓ Encontrado en ${maze.getStart()}")
        println("   • E unico: ✓ Encontrado en ${maze.getEnd()}")
        println("   • Tamaño mínimo: ✓ ${maze.rows}x${maze.cols} (≥ 10x10)")
        println("   • Vida inicial: ✓ ${maze.initialHealth} (≥ 10)")
        
        // 3. Representación interna de la matriz
        println("\n3. 🗺️  REPRESENTACION INTERNA")
        println("-".repeat(40))
        println("   • Total de celdas: ${maze.rows * maze.cols}")
        println("   • Movimientos válidos: 4-direcciones")
        println("   • Sistema de coordenadas: (fila, columna)")
        
        // Contar tipos de celdas
        val cellCounts = mutableMapOf<Char, Int>()
        for (i in 0 until maze.rows) {
            for (j in 0 until maze.cols) {
                val cell = maze.grid[i][j]
                cellCounts[cell] = cellCounts.getOrDefault(cell, 0) + 1
            }
        }
        println("   • Distribucion de celdas:")
        cellCounts.entries.sortedBy { it.key }.forEach { (char, count) ->
            val description = when (char) {
                '#' -> "Paredes"
                'S' -> "Start"
                'E' -> "End" 
                'T' -> "Tesoros"
                in '0'..'9' -> "Costo $char"
                else -> "Espacios"
            }
            println("     - '$char': $count ($description)")
        }
        
        // 4. BFS simple que busca E ignorando vida y tesoros
        println("\n4. 🔍 VERIFICACION DE CONECTIVIDAD (BFS)")
        println("-".repeat(40))
        val solver = MazeSolver(maze)
        
        // Análisis completo de conectividad
        val solution = solver.solveBasicConnectivity()
        val analysis = solver.analyzeConnectivity()
        
        println("   • Componentes conexos encontrados: ${analysis.componentCount}")
        println("   • Celdas en componente de S: ${analysis.startComponent?.size ?: 0}")
        println("   • Celdas en componente de E: ${analysis.endComponent?.size ?: 0}")
        
        if (solution.isPossible) {
            println("   • Conectividad: ✅ S y E CONECTADOS")
            println("   • Pasos mínimos (estructura): ${solution.steps}")
            println("   • Camino más corto (ignorando vida/tesoros):")
            solution.optimalPath?.forEachIndexed { index, pos ->
                println("       Paso $index: $pos")
            }
        } else {
            println("   • Conectividad: ❌ S y E NO CONECTADOS")
            println("   • Razón: ${solution.message}")
        }
        
        // 5. Detección temprana de casos imposibles por paredes
        println("\n5. 🚧 DETECCIÓN DE CASOS IMPOSIBLES")
        println("-".repeat(40))
        
        if (!analysis.isConnected) {
            println("   ❌ PROBLEMA ESTRUCTURAL DETECTADO")
            println("   • S y E están en componentes conexos diferentes")
            println("   • Laberinto IMPOSIBLE por configuración de paredes")
            println("   • No existe camino físico de S a E")
        } else {
            println("   ✅ ESTRUCTURA VÁLIDA")
            println("   • S y E están en el mismo componente conexo")
            println("   • Existe al menos un camino físico de S a E")
        }
        
        val allValid = solution.isPossible && 
                      analysis.isConnected && 
                      maze.rows >= 10 && 
                      maze.cols >= 10 && 
                      maze.initialHealth >= 10
        
        if (allValid) {
            println("✅ LABERINTO VÁLIDO IGNORANDO TESOROS Y VIDAS")
            println("✅ Cumple todos los requisitos estructurales")
        } else {
            println("❌ LABERINTO INVÁLIDO")
            if (!solution.isPossible) {
                println("❌ Problema: E inalcanzable desde S")
            }
            if (maze.rows < 10 || maze.cols < 10) {
                println("❌ Problema: Tamaño menor a 10x10")
            }
            if (maze.initialHealth < 10) {
                println("❌ Problema: Vida inicial menor a 10")
            }
        }
        
        println("\n📊 ESTADÍSTICAS:")
        println("   • Dimensión: ${maze.rows} x ${maze.cols}")
        println("   • Vida inicial: ${maze.initialHealth}")
        println("   • Conectividad: ${if (solution.isPossible) "SI" else "NO"}")
        println("   • Pasos mínimos: ${solution.steps}")
        println("   • Componentes conexos: ${analysis.componentCount}")
        
    } catch (e: Exception) {
        println("\n❌ ERROR DURANTE LA VERIFICACIÓN")
        println("   Mensaje: ${e.message}")
        println("   El archivo puede tener formato incorrecto")
    }
}