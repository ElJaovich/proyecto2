package ve.usb.libGrafo

class GrafoDirigidoCosto : GrafoDirigidoBase {
    constructor(numVertices: Int) : super(conCosto = true) {
        require(numVertices >= 0) { "El número de vértices debe ser no negativo: $numVertices" }
        v = numVertices
        for (i in 0 until v) {
            listaAdy[i] = mutableListOf()
        }
    }

    constructor(fuente: String) : super(fuente, conCosto = true)

    fun agregarArco(arco: ArcoCosto) {
        validarVertice(arco.u)
        validarVertice(arco.v)
        agregarLado(arco)
    }

    fun agregarArco(u: Int, v: Int, costo: Double) {
        validarVertice(u)
        validarVertice(v)
        agregarLado(ArcoCosto(u, v, costo))
    }
}