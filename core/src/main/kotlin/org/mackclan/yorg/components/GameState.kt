package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.viewport.FitViewport

class GameState : Component {
    val viewport by lazy { FitViewport(20f, 20f) }
    var playerTurn = true

    var selected: Entity? = null
}
