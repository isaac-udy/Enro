package nav.enro.result

interface EnroResultChannel<T> {
    fun open(key: ResultNavigationKey<T>)
}