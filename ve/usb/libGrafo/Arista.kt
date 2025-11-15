package ve.usb.libGrafo

import kotlin.random.Random

open class Arista(u: Int, v: Int) : Lado(u, v) {
    fun cualquierVertice(): Int = if (Random.nextBoolean()) u else v
    override fun vecino(x: Int): Int = when (x) {
        u -> v
        v -> u
        else -> throw IllegalArgumentException("El vértice $x no pertenece a la arista ($u,$v).")
    }

    override fun toString(): String = "($u,$v)"
}