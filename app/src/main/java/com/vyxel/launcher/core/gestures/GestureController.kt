package com.vyxel.launcher.core.gestures

import com.vyxel.launcher.core.model.GestureAction
import com.vyxel.launcher.core.model.GestureMap
import com.vyxel.launcher.core.model.GestureTrigger

class GestureController {
    fun resolve(map: GestureMap, trigger: GestureTrigger): GestureAction =
        map.actionFor(trigger).action

    fun appFor(map: GestureMap, trigger: GestureTrigger): String? =
        map.actionFor(trigger).appComponent

    companion object {
        const val SWIPE_THRESHOLD = 72f
        const val FAST_SWIPE = 140f
    }
}
