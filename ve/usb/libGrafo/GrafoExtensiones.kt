package ve.usb.libGrafo

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val DATETIME_FORMAT = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy HH:mm:ss")

fun obtenerTxtEnDirectorio(directorioPath: String): List<File> {
    val directorio = File(directorioPath)

    if (!directorio.exists() || !directorio.isDirectory) {
        println("Error: El directorio $directorioPath no existe")
        return emptyList()
    }

    val archivos = directorio.listFiles { it.extension.equals("txt", ignoreCase = true) }
        ?.sortedBy { it.name }
        ?: emptyList()

    when {
        archivos.isEmpty() -> println("No se encontraron archivos .txt en $directorioPath")
        else -> println(
            "${
                LocalDateTime.now().format(DATETIME_FORMAT)
            } - Archivos en $directorioPath: ${archivos.joinToString(", ") { it.name }}"
        )
    }

    return archivos
}

object GrafoFactory {
    private val TIPOS_VALIDOS = setOf(
        "dirigido",
        "no_dirigido",
        "dirigido_costo",
        "no_dirigido_costo"
    )

    // Crea un grafo desde un archivo que contiene la especificación del tipo.
    fun desdeArchivo(ruta: String): Grafo {
        val archivo = File(ruta)

        // Validar que el archivo existe
        require(archivo.exists()) {
            "El archivo no existe: $ruta"
        }
        require(archivo.isFile) {
            "La ruta no es un archivo: $ruta"
        }

        // Buscar la línea tipo=...
        val tipo = archivo.useLines { lineas ->
            lineas
                .map { it.substringBefore("#").trim() }  // Remover comentarios
                .filter { it.isNotBlank() }
                .firstOrNull { it.startsWith("tipo=") }
                ?.substringAfter("tipo=")
                ?.trim()
                ?.lowercase()
                ?.replace(" ", "_")  // "no dirigido" -> "no_dirigido"
        } ?: throw IllegalArgumentException(
            "Falta especificar 'tipo=' en el archivo: $ruta\n" +
                    "Tipos válidos: ${TIPOS_VALIDOS.joinToString(", ")}"
        )

        // Validar que el tipo es válido
        require(tipo in TIPOS_VALIDOS) {
            "Tipo desconocido: '$tipo' en archivo: $ruta\n" +
                    "Tipos válidos: ${TIPOS_VALIDOS.joinToString(", ")}"
        }

        // Crear el grafo según el tipo
        return when (tipo) {
            "dirigido" -> GrafoDirigido(ruta)
            "no_dirigido" -> GrafoNoDirigido(ruta)
            "dirigido_costo" -> GrafoDirigidoCosto(ruta)
            "no_dirigido_costo" -> GrafoNoDirigidoCosto(ruta)
            else -> throw IllegalStateException("Tipo validado pero no implementado: $tipo")
        }
    }

    // Crea un grafo desde un string con el contenido y tipo especificado.
    fun desdeString(contenido: String, tipoExplicito: String? = null): Grafo {
        val tipo = tipoExplicito?.lowercase()?.replace(" ", "_")
            ?: contenido.lines()
                .map { it.substringBefore("#").trim() }
                .filter { it.isNotBlank() }
                .firstOrNull { it.startsWith("tipo=") }
                ?.substringAfter("tipo=")
                ?.trim()
                ?.lowercase()
                ?.replace(" ", "_")
            ?: throw IllegalArgumentException(
                "Debe especificar tipo explícitamente o incluir 'tipo=' en el contenido\n" +
                        "Tipos válidos: ${TIPOS_VALIDOS.joinToString(", ")}"
            )

        require(tipo in TIPOS_VALIDOS) {
            "Tipo desconocido: '$tipo'\n" +
                    "Tipos válidos: ${TIPOS_VALIDOS.joinToString(", ")}"
        }

        return when (tipo) {
            "dirigido" -> GrafoDirigido(contenido)
            "no_dirigido" -> GrafoNoDirigido(contenido)
            "dirigido_costo" -> GrafoDirigidoCosto(contenido)
            "no_dirigido_costo" -> GrafoNoDirigidoCosto(contenido)
            else -> throw IllegalStateException("Tipo validado pero no implementado: $tipo")
        }
    }

    fun tiposSoportados(): Set<String> = TIPOS_VALIDOS
}