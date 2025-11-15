package ve.usb.libGrafo

class GrafoDirigido : GrafoDirigidoBase {
    constructor(numVertices: Int) : super(conCosto = false) {
        require(numVertices >= 0) { "El número de vértices debe ser no negativo: $numVertices" }
        v = numVertices
        for (i in 0 until v) {
            listaAdy[i] = mutableListOf()
        }
    }

    constructor(fuente: String) : super(fuente, conCosto = false)

    fun agregarArco(arco: Arco) {
        validarVertice(arco.u)
        validarVertice(arco.v)
        agregarLado(arco)
    }

    fun agregarArco(u: Int, v: Int) {
        validarVertice(u)
        validarVertice(v)
        agregarLado(Arco(u, v))
    }
}