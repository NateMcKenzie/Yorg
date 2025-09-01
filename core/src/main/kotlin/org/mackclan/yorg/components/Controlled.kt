package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component

class Controlled (val walkRange : Int, var playerControlled : Boolean): Component{
    var selected : Boolean = false
}
