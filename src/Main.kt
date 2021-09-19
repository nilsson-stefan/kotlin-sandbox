package se.beatit

import java.util.*
import kotlin.concurrent.thread
import kotlin.properties.Delegates
import kotlin.reflect.KProperty




fun main() {
    println("Hello")

    val adventure = Category(CategoryCode.ADVENTURE, "Very exciting shit")

    val book1 = Book("The Book", category = adventure)

    println("Book title ${book1.title}")
    println("Book author(s) ${book1.authors}")
    println("Book description ${book1.description}")
    println("Book price ${book1.price}")

    println("Book price amount ${book1.price?.amount}")
    book1.price?.amount.let { println("Book price amount $it") }

    // println("Book price amount ${book.price!!.amount}") // Throws exception

    val book2 = Book("Book 2", "Other secret")

    val book3 = Book("Book 3", listOf("Stefan"), adventure, "Bra bok",
            ReaPrice(20.0, Currency.getInstance("SEK")))
    println("Price of book 3 is ${book3.price}")
    println("aAAA ${book3.category}")

    val t = thread {
        Thread.sleep(2000)
        when (val result = findBook()) {
            is Success -> println("Success $result.book on separate thread")
            is Error -> print("Error $result.message on separate thread")
        }
    }

    println("Const ${Book.staticvalue}")

    val darkRedColour = DarkColour(Red())
    println("Colour is ${darkRedColour.getBrightness()} ${darkRedColour.getColour()}")

    println("Book3 secret2 before secret2 is set ${book3.secret2}")
    book3.secret2 = "HAHAH"
    println("Book3 has delegated secret2 ${book3.secret2}")

    println("Getting the lazy property of book3 ${book3.lazyProperty}")
    println("Getting it again ${book3.lazyProperty}")
    println("Getting it again ${book3.lazyProperty}")

    println("Getting the observed property of book3 ${book3.observedProperty}")
    book3.observedProperty = "New value"

    fun Book.printTitle() { // Extension function
        println("Extension function is printing the title $title")
    }
    book1.printTitle()

    fun Price.printAmount() {
        println("Extension function of Price printing amount $amount")
    }
    ReaPrice(100.0, Currency.getInstance("SEK")).printAmount() // Extension function on parent class

    println("Getting extension property ${book1.substituteTitle}") // Extension property

    val redColourBucket = BucketWithColor(Red())
    val greenColourBucket = BucketWithColor(Green())
    listOf(redColourBucket, greenColourBucket)
            .asSequence().forEach { printColor(it) { it * 2 } } // printColor with trailing lambda (i -> i replaced with it)

    val sum: (Int, Int) -> Int = { a: Int, b: Int -> a + b } // lambda as value
    passLambdaAsParameter(sum)

    getGenericBucket(redColourBucket, { println("The bucket contiains red colour") }, book1::lambdaTest, ::blabla)

    book1.varargstest("va1", "va2", "va3", anotherarg = 5)

    println("Infix extension function says that 3 > 2 is ${3 gt 2} and ${"bbb" addAnthing "ccc"}")
    book3 withThirdSecret "so secret"
    print("Book three gor third secret (${book3.secret3}) from infix function")

    mainCollections()

    t.join(1500)
    println("Out")
}

fun passLambdaAsParameter(sum: (Int, Int) -> Int) {
    println("Use lambda to sum 3 and 2 = ${sum(3, 2)}")
}

fun blabla() {
    print("Bla bla")
}

infix fun Int.gt(x: Int): Boolean {
    return this > x
} // Infix on extension function

infix fun String.addAnthing(s: String): String {
    return "$this $s"
}

// Higher order function take function as parameter or returns one
// Generics "out" or "in" makes "inheritance" in generic type possible
fun printColor(bucketWithColour: BucketWithColor<out Colour>, dodouble: (Int) -> Int) {
    println("Contains ${bucketWithColour.colour.getColour()} and doubleit ${dodouble(3)}")
}

// Generics function with out
fun <T : BucketWithColor<out Colour>> getGenericBucket(t: T,
                                                       lambda1: () -> Unit,
                                                       lambda2: () -> Unit,
                                                       lambda3: () -> Unit): T =
        if (t.colour is Red) {
            lambda1()
            t
        } else {
            lambda2()
            lambda3()
            t
        }


fun findBook(): Result {
    return Success(Book("Book 2", "Other secret"))
}

var Book.substituteTitle: String // Extension property (no backing field, cant hold value, just getter and setter
    get() = title
    set(value) {
        // title = value
    }

class Book(val title: String, val authors: List<String> = emptyList(),
           val category: Category,
           val description: String? = null, val price: Price? = null) {

    private var secret: String = "Secret $title"
        get() = field.toUpperCase()
        set(value) {
            println("Setting secret to $value")
            field = value
        }

    var secret2: String by Secret2Delegate() // property delegation ("by")
    var secret3: String? = null

    val lazyProperty: String by lazy { // Standard "lazy" property delegate
        println("This is the first call and we are doing alot of heavy calculations to get the value")
        "The lazy property"
    }

    var observedProperty: String by Delegates.observable("Initial value") { prop, old, new ->
        println("Observing old value: $old -> new value: $new")
    }

    init {
        println("init $secret")
    }

    constructor(title: String, s: String) : this(title, category = Category(CategoryCode.UNKNOWN, "Unknown")) {
        secret = s
    }

    companion object {
        val staticvalue = "A static value"
    }

    infix fun withThirdSecret(s: String) {
        secret3 = s
    }

    fun lambdaTest() {
        println("The bucket contiains green colour")
    }

    fun varargstest(vararg args: String, anotherarg: Int) {
        for (arg in args) {
            println(arg)
        }
    }
}

class Secret2Delegate { // property delegate for secret2
    lateinit var theValue: String

    operator fun getValue(book: Book, property: KProperty<*>): String {
        if (this::theValue.isInitialized) {
            return theValue
        } else {
            println("This lateinit variable has not been set yet!!!")
            return "not set"
        }
    }

    operator fun setValue(book: Book, property: KProperty<*>, string: String) {
        theValue = "$string by delegate for book $book"
    }

}

open class Price(open val amount: Double, val currency: Currency) { // "open" to enable inheritance
    override fun toString() = "$amount ${currency.displayName}"
}

class ReaPrice(a: Double, currency: Currency) : Price(a, currency) {
    override val amount: Double = a / 2

}

data class Category(val categoryCode: CategoryCode, val description: String) // Data class

enum class CategoryCode(val title: String) {
    ADVENTURE("Adventure"),
    UNKNOWN("Unknown")
}

const val reaprice: String = "REA PRICE"


sealed class Result()   // Sealed classes
class Success(val book: Book) : Result()
class Error(val message: String) : Result()

interface Colour {
    fun getColour(): String
    fun getBrightness(): String
}

class Red : Colour {
    override fun getColour() = "Red"
    override fun getBrightness() = "Medium"
}

class Green : Colour {
    override fun getColour() = "Green"
    override fun getBrightness() = "Medium"
}

class DarkColour(colour: Colour) : Colour by colour { // Delegation ("by")
    override fun getBrightness() = "Dark"
}

class BucketWithColor<Colour>(val colour: Colour)


/////////////// Collectons

fun mainCollections() {

    // Collection
    println(listOf(1, 2, 3)) // [1, 2, 3]
    println(mutableSetOf(1, 2, 3)) // [1, 2, 3]
    println(ArrayList<Int>()) // empty ArrayList

    // List
    val list = listOf(1, 2, 3)
    val mutableList = mutableListOf(1, 2, 3)
    mutableList.add(4)
    val arrayList = ArrayList<Int>()
    val emptyList = emptyList<Int>()

    // Set
    val s = setOf(1, 2, 3)
    val hashSet = HashSet<Int>()
    hashSet.add(4)
    println(hashSet) // [4]

    // Map
    val m = mapOf(1 to "one", 2 to "two")
    val hashMap = HashMap<Int, String>(m)
    hashMap.put(3, "three")
    println(hashMap) // {1=one, 2=two, 3=three}

    val fromList = mutableListOf(1, 2, 3)
    val toListByCopy = fromList.toList()
    val toListByRef = fromList
    fromList.add(4)
    println("from $fromList toListByCopy $toListByCopy toListByRef $toListByRef")

    val testList = listOf(1, 2, 3, 4, 5, 6)
    if(2 in testList) {
        println("Yes 2 is in list")
    }

    val p = testList.partition { it > 3 } // get both mathing and not motching in a Pair<List,List>
    println("Large ${p.theBigOnes()} Small ${p.theSmallOnes()}")

    val l = listOf(SimpleClass("a", 1), SimpleClass("b", 2), SimpleClass("c", 3))
    println(l.associateBy { it.nmb })
    println(l.associate { Pair(it.nmb, it) })
    println(l.associateWith { it.str.toUpperCase() })

    println(list.groupBy{ if(it%2==0) "Even" else "Odd" })
    println(list.groupingBy{ if(it%2==0) "Even" else "Odd" }.eachCount())

    println(list.groupingBy{ if(it%2==0) "Even" else "Odd" }
            .reduce{ key, total, e -> total + e})

    println(list.groupingBy{ if(it%2==0) "Even" else "Odd" }
            .fold(2){total, e -> total + e})

    println(list.groupingBy{ if(it%2==0) "Even" else "Odd" }
            .aggregate{ k, total: Int? , e, b ->
                println("k=$k e=$e total=$total b=$b");
                if(b) e else total?.plus(e)
            })
}

fun Pair<Any,Any>.theBigOnes(): Any { return this.first }
fun Pair<Any,Any>.theSmallOnes(): Any { return this.second }

data class SimpleClass(val str: String, val nmb: Int)

val list = listOf(1, 2, 3, 4, 5)


