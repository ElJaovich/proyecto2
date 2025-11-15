package ve.usb.libGrafo

abstract class Lado(val u: Int, val v: Int) {
    abstract override fun toString(): String
    abstract fun vecino(x: Int): Int
}