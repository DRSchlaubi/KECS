package com.github.quillraven.kecs

import com.badlogic.gdx.utils.Array
import kotlin.reflect.KClass

class KECSManager(
    initialEntityCapacity: Int = 100,
    initialComponentCapacity: Int = 20
) {
    private val componentManager = KECSComponentManager(initialEntityCapacity, initialComponentCapacity)
    private val familyManager = KECSFamilyManager(componentManager)
    private val entityManager = KECSEntityManager(componentManager, initialEntityCapacity).apply {
        // important to notify component manager before family manager because component data is needed
        // by families and therefore it needs to be updated first
        addListener(componentManager)
        addListener(familyManager)
    }

    private val allOfDSL = Array<KECSComponentMapper>()
    private val noneOfDSL = Array<KECSComponentMapper>()
    private val anyOfDSL = Array<KECSComponentMapper>()
    private val familyDSL = KECSFamilyDSL(allOfDSL, noneOfDSL, anyOfDSL, componentManager)

    private val componentsDSL = Array<KECSComponent>(false, initialComponentCapacity)
    private val entityDSL = KECSEntityCreateDSL(componentManager, componentsDSL)

    fun entity(init: KECSEntityCreateDSL.() -> Unit = {}): KECSEntity {
        entityDSL.run {
            componentsDSL.clear()
            init()
            return entityManager.obtain(componentsDSL)
        }
    }

    operator fun contains(entity: KECSEntity) = entity in entityManager

    inline fun <reified T : KECSComponent> mapper(): KECSComponentMapper = mapper(T::class)

    fun mapper(type: KClass<out KECSComponent>) = componentManager.mapper(type)

    fun family(init: KECSFamilyDSL.() -> Unit): KECSFamily {
        familyDSL.run {
            allOfDSL.clear()
            noneOfDSL.clear()
            anyOfDSL.clear()
            init()
            return familyManager.family(allOfDSL, noneOfDSL, anyOfDSL, entityManager.entities.size)
        }
    }
}
