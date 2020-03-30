package inc.ahmedmourad.sherlock.domain.constants

import inc.ahmedmourad.sherlock.domain.platform.TextManager

interface ValuedEnum<V> {
    val value: V
}

enum class Gender(override val value: Int) : ValuedEnum<Int> {
    MALE(0) {
        override fun getMessage(textManager: TextManager): String {
            return textManager.male()
        }
    },
    FEMALE(1) {
        override fun getMessage(textManager: TextManager): String {
            return textManager.female()
        }
    };

    abstract fun getMessage(textManager: TextManager): String
}

enum class Hair(override val value: Int) : ValuedEnum<Int> {
    BLONDE(0) {
        override fun getMessage(textManager: TextManager): String {
            return textManager.blondeHair()
        }
    },
    BROWN(1) {
        override fun getMessage(textManager: TextManager): String {
            return textManager.brownHair()
        }
    },
    DARK(2) {
        override fun getMessage(textManager: TextManager): String {
            return textManager.darkHair()
        }
    };

    abstract fun getMessage(textManager: TextManager): String
}

enum class Skin(override val value: Int) : ValuedEnum<Int> {
    WHITE(0) {
        override fun getMessage(textManager: TextManager): String {
            return textManager.whiteSkin()
        }
    },
    WHEAT(1) {
        override fun getMessage(textManager: TextManager): String {
            return textManager.wheatishSkin()
        }
    },
    DARK(2) {
        override fun getMessage(textManager: TextManager): String {
            return textManager.darkSkin()
        }
    };

    abstract fun getMessage(textManager: TextManager): String
}

inline fun <V, reified T : ValuedEnum<V>> findEnum(value: V, enumValues: Array<T>): T {

    for (item in enumValues)
        if (value == item.value)
            return item

    throw IllegalArgumentException("$value is not a valid error of type ${T::class.java.canonicalName}")
}
