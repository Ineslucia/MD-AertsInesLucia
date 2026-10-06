//if bid == null, there is no bidder, so the item must be sold at the minimum price.
//then return the bid.amount.
//if bid exists -> return bid.amount
//if bid is null -> return minimumPrice



fun main() {
    val winningBid = Bid(5000, "Private Collector")

    println("Item A is sold at ${auctionPrice(winningBid, 2000)}.")
    println("Item B is sold at ${auctionPrice(null, 3000)}.")
}

class Bid(val amount: Int, val bidder: String)

fun auctionPrice(bid: Bid?, minimumPrice: Int): Int {
    return if (bid != null) {
        bid.amount
    } else {
        minimumPrice
    }
}

