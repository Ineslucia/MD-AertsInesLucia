// Ticket prices with age
//Children (under 13) = $15
//Adults (13 or over, but under 60) = $30 but $25 on Monday
//Seniors (61 or over) = $20
//invalid ages = return -1 (age < 0 || age > 100)


fun main() {
    val child = 5
    val adult = 28
    val senior = 87

    val isMonday = true

    println("The movie ticket price for a person aged $child is \$${ticketPrice(child, isMonday)}.")
    println("The movie ticket price for a person aged $adult is \$${ticketPrice(adult, isMonday)}.")
    println("The movie ticket price for a person aged $senior is \$${ticketPrice(senior, isMonday)}.")
}

fun ticketPrice(age: Int, isMonday: Boolean): Int {

    // Invalid ages
    if (age < 0 || age > 100) {
        return -1
    }

    // Child price
    if (age <= 12) {
        return 15
    }

    // Adult price
    if (age in 13..60) {
        return if (isMonday) 25 else 30
    }

    // Senior price
    return 20
}
