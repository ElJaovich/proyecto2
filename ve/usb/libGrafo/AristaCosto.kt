package ve.usb.libGrafo

class AristaCosto(u: Int, v: Int, val costo: Double) : Arista(u, v) {
    fun costo(): Double = costo
    override fun toString(): String = "($u,$v,$costo)"
}