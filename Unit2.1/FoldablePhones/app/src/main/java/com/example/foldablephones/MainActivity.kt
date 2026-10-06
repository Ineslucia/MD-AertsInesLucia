//A normal phone screen turns on/off with the power button
//A foldable phone does NOT turn on when folded
//it must inherit from the Phone Class
//Class must include:
//A property indicating whether the phone is folded
//A different switchOn() behavior so the screen only turns on when NOT folded
//Methods to change the folding state (fold/unfold)


package com.example.foldablephones

class FoldablePhone : Phone() {

    var isFolded: Boolean = false

    override fun switchOn() {
        if (!isFolded) {
            super.switchOn()
        }
    }

    fun fold() {
        isFolded = true
    }

    fun unfold() {
        isFolded = false
    }
}



open class Phone(var isScreenLightOn: Boolean = false){
    open fun switchOn() {
        isScreenLightOn = true
    }

    fun switchOff() {
        isScreenLightOn = false
    }

    fun checkPhoneScreenLight() {
        val phoneScreenLight = if (isScreenLightOn) "on" else "off"
        println("The phone screen's light is $phoneScreenLight.")
    }
}



fun main() {
    val fp = FoldablePhone()

    fp.fold()
    fp.switchOn()
    fp.checkPhoneScreenLight()   // stays off

    fp.unfold()
    fp.switchOn()
    fp.checkPhoneScreenLight()   // turns on

    fp.switchOff()
    fp.checkPhoneScreenLight()   // turns off
}
