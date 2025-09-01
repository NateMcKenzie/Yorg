package org.mackclan.yorg.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.viewport.FitViewport

class BoolCallback (val callback: (Boolean) -> Unit): Component
