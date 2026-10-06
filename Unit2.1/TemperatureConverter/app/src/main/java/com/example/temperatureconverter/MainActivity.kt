//Celsius to Fahrenheit: ° F = 9/5 (° C) + 32
//Kelvin to Celsius: ° C = K - 273.15
//Fahrenheit to Kelvin: K = 5/9 (° F - 32) + 273.15


fun main() {
    println(fahrenheitToCelsius(68.0))
    println(celsiusToFahrenheit(20.0))
    println(celsiusToKelvin(0.0))
}

fun fahrenheitToCelsius(fahrenheit: Double): Double {
    return (fahrenheit - 32) * 5 / 9
}

fun celsiusToFahrenheit(celsius: Double): Double {
    return celsius * 9 / 5 + 32
}

fun celsiusToKelvin(celsius: Double): Double {
    return celsius + 273.15
}




fun printFinalTemperature(
    initialMeasurement: Double,
    initialUnit: String,
    finalUnit: String,
    conversionFormula: (Double) -> Double
) {
    val finalMeasurement = String.format("%.2f", conversionFormula(initialMeasurement)) // two decimal places
    println("$initialMeasurement degrees $initialUnit is $finalMeasurement degrees $finalUnit.")
}