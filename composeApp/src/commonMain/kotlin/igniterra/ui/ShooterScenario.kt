package igniterra.ui

// ── Scénario du shooter ───────────────────────────────────────────────────────
// La mécanique est commune ; seuls les textes et quelques options changent.
class ShooterScenario(
    val hudTitle          : String,
    val civiliansLabel    : String,
    val savedLabel        : String,
    val lostLabel         : String,
    val endSavedLabel     : String,
    val endLostLabel      : String,
    val civilianSaved     : String,
    val civilianSavedLife : String,
    val civilianLost      : String,
    val civilianKilled    : String,
    val killedMessageRed  : Boolean,
    val firstLoss         : String,
    val bossThrows        : String,
    val bossImmune        : String,       // doit contenir "REQUIS" (couleur)
    val levelUp           : List<String>, // paliers 1, 2, 3
    val weaponLabels      : List<String>, // paliers 0, 1, 2, 3
    val partsUnit         : String,
    val maxLabel          : String,
    val victoryButton     : String,
    val bitterButton      : String,
    val abandonButton     : String,
    val maxAttempts       : Int? = null,  // null = illimité
    val adultCivilian     : Boolean = false,
)

val IgniTerraScenario = ShooterScenario(
    hudTitle          = "IGNI TERRA — MODE COMBAT",
    civiliansLabel    = "CIVILES",
    savedLabel        = "SAUVÉES",
    lostLabel         = "PERDUES",
    endSavedLabel     = "Sauvees",
    endLostLabel      = "Perdues",
    civilianSaved     = "SAUVÉE ! +50",
    civilianSavedLife = "SAUVEE ! +50 +VIE",
    civilianLost      = "UNE ENFANT EST PERDUE...",
    civilianKilled    = "TU L'AS TUÉE !",
    killedMessageRed  = false,
    firstLoss         = "Mes condoleances, tu viens de tuer Elea !",
    bossThrows        = "!! IL LANCE UNE ENFANT !!",
    bossImmune        = "!! IGNI TERRA γ REQUIS !!",
    levelUp           = listOf("MODE α DEBLOQUE !", "MODE β DEBLOQUE !", "MODE γ — PUISSANCE MAX !"),
    weaponLabels      = listOf("IGNI TERRA", "IGNI TERRA α", "IGNI TERRA β", "IGNI TERRA γ MAX"),
    partsUnit         = "pieces",
    maxLabel          = "PUISSANCE MAX",
    victoryButton     = "Felicitation ! Viens ici !",
    bitterButton      = "On se decourage ? Viens ici !",
    abandonButton     = "On abandonne ? Viens ici.",
)
