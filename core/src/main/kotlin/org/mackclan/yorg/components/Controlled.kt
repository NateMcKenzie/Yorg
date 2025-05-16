package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component

class Controlled (val range : Int, var playerControlled : Boolean ): Component{
    var selected : Boolean = false
}
