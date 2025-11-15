package ve.usb.libGrafo

class GrafoNoDirigidoCosto : GrafoNoDirigidoBase {
    constructor(numVertices: Int) : super(conCosto = true) {
        require(numVertices >= 0) { "El número de vértices debe ser no negativo: $numVertices" }
        v = numVertices
        for (i in 0 until v) {
            listaAdy[i] = mutableListOf()
        }
    }

    constructor(fuente: String) : super(fuente, conCosto = true)

    fun agregarArista(arista: AristaCosto) {
        validarVertice(arista.u)
        validarVertice(arista.v)
        agregarLado(arista)
    }

    fun agregarArista(u: Int, v: Int, costo: Double) {
        validarVertice(u)
        validarVertice(v)
        agregarLado(AristaCosto(u, v, costo))
    }
}