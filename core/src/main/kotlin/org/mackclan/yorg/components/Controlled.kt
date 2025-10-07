package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component

class Controlled (val walkRange : Int, var playerControlled : Boolean): Component{
    var selected : Boolean = false
    var coverUp : CoverLevel = CoverLevel.None
    var coverRight : CoverLevel = CoverLevel.None
    var coverDown : CoverLevel = CoverLevel.None
    var coverLeft : CoverLevel = CoverLevel.None
}
