package ve.usb.libGrafo

class ArcoCosto(u: Int, v: Int, val costo: Double) : Arco(u, v) {
    fun costo(): Double = costo
    override fun toString(): String = "($u→$v,$costo)"
}