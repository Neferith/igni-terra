package com.garlean.igniterra.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

// ─── Données du manuel ─────────────────────────────────────────────────────────

data class SpecEntry(val label: String, val value: String)
data class FireMode(val id: String, val name: String, val range: String, val description: String, val effect: String, val warning: String?)

val SPECS = listOf(
    SpecEntry("DÉSIGNATION",       "Igni Terra · Lance-Flamme Magitek Mk. I"),
    SpecEntry("CLASSIFICATION",    "Arme de Destruction Ciblée — Usage Restreint"),
    SpecEntry("OPÉRATEURS",        "Damian [BITS] · Eleanor"),
    SpecEntry("PORTÉE NOMINALE",   "5 à 15 yalms (configurable)"),
    SpecEntry("SOURCE ÉNERGÉTIQUE","Cristal de Foudre (logement caoutchouc traité)"),
    SpecEntry("PROPULSEUR",        "Amalgame trisélément [voir §3.2]"),
    SpecEntry("ALLUMAGE",          "Arc électrique double-électrode"),
    SpecEntry("CONTRÔLE PUISSANCE","Résistance variable mécanique — gâchette progressive"),
    SpecEntry("RÉCUPÉRATION",      "Réinjection partielle du champ EM résiduel"),
    SpecEntry("AUTONOMIE",         "Améliorée — système boucle fermée"),
    SpecEntry("TEMPÉRATURE MAX",   "Non définie — dissipateur requis pour usage prolongé"),
    SpecEntry("STATUT",            "Prototype Mk.I — Approuvé aux tests")
)

val FIRE_MODES = listOf(
    FireMode(
        id = "01",
        name = "MODE COURT",
        range = "~5 yalms",
        description = "Pression minimale sur la gâchette. Jet