package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component

class Movable (val range : Int): Component{
    var selected : Boolean = false
}
