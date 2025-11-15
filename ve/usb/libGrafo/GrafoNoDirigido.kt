package ve.usb.libGrafo

class GrafoNoDirigido : GrafoNoDirigidoBase {
    constructor(numVertices: Int) : super(conCosto = false) {
        require(numVertices >= 0) { "El número de vértices debe ser no negativo: $numVertices" }
        v = numVertices
        for (i in 0 until v) {
            listaAdy[i] = mutableListOf()
        }
    }

    constructor(fuente: String) : super(fuente, conCosto = false)

    fun agregarArista(arista: Arista) {
        validarVertice(arista.u)
        validarVertice(arista.v)
        agregarLado(arista)
    }

    fun agregarArista(u: Int, v: Int) {
        validarVertice(u)
        validarVertice(v)
        agregarLado(Arista(u, v))
    }
}