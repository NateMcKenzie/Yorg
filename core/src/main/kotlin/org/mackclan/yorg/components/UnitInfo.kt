package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.viewport.FitViewport

class UnitInfo : Component{
    var health = 10
    var damage = 10
    var range = 10
}
