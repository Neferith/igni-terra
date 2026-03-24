package igniterra

/**
 * Déclaration commune — l'implémentation est dans chaque sourceSet cible.
 * Desktop : javax.sound.sampled (sons procéduraux)
 */
expect object CrackleSound {
    fun start()
    fun stop()
    fun click()
    fun openDocument()

    fun unlockSecret()
    fun snakeEat()
    fun snakeDie()
    fun snakeMusicStart()
    fun snakeMusicStop()
    fun setVolume(volume: Float)  // 0.0 = muet, 1.0 = max

    fun playWav(filename: String, loop: Boolean = false)
    fun stopWav()
}