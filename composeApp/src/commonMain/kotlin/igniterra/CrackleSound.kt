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

    // Donjon
    fun dungeonHit()
    fun dungeonEnemyDie()
    fun dungeonItemPickup()
    fun dungeonLevelUp()
    fun dungeonGameOver()
    fun dungeonVictory()

    fun laugh()


    // Shooter
    fun shooterFleshHit()      // monstre touché — morceau arraché
    fun shooterGirlSaved()     // petite fille sauvée
    fun shooterGirlKilled()    // petite fille tuée
    fun shooterPartPickup()    // pièce Igni Terra ramassée
    fun shooterPlayerHit()     // monstre dans notre camp

    fun shooterLevelUp()        // montée de niveau Igni Terra
}