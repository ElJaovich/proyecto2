package ve.usb.libGrafo

open class Arco(u: Int, v: Int) : Lado(u, v) {
    fun origen(): Int = u // Retorna el vértice inicial del arco
    fun destino(): Int = v // Retorna el vértice final del arco
    override fun vecino(x: Int): Int = when (x) {
        u -> v  // solo tiene sentido desde el origen hacia el destino
        else -> throw IllegalArgumentException("El vértice $x no es el origen del arco ($u→$v).")
    }

    override fun toString(): String = "($u→$v)"
}