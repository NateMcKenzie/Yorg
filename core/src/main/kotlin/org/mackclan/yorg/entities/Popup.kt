package org.mackclan.yorg.entities

import com.badlogic.ashley.core.Entity
import org.mackclan.yorg.components.BoolCallback
import org.mackclan.yorg.components.FreshFlag
import org.mackclan.yorg.components.ShotInfo

fun createPopup(damage: Int, chance: Float, callback: (Boolean) -> Unit): Entity {
    val popup = Entity()
    popup.add(FreshFlag())
    popup.add(ShotInfo(chance, damage))
    popup.add(BoolCallback(callback))
    return popup
}
