package com.github.quillraven.kecs

import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.OrderedSet
import java.util.*
import kotlin.reflect.KClass

@DslMarker
annotation class FamilyDsl

@FamilyDsl
class FamilyBuilder(
    private val world: World,
    private val families: ObjectSet<Family>
) {
    private val allOf = OrderedSet<ComponentManager<*>>().apply {
        orderedItems().ordered = false
    }
    private val noneOf = OrderedSet<ComponentManager<*>>().apply {
        orderedItems().ordered = false
    }
    private val anyOf = OrderedSet<ComponentManager<*>>().apply {
        orderedItems().ordered = false
    }

    fun allOf(vararg componentTypes: KClass<*>) {
        componentTypes.forEach {
            allOf.add(world.componentManager(it.java))
        }
    }

    fun noneOf(vararg componentTypes: KClass<*>) {
        componentTypes.forEach {
            noneOf.add(world.componentManager(it.java))
        }
    }

    fun anyOf(vararg componentTypes: KClass<*>) {
        componentTypes.forEach {
            anyOf.add(world.componentManager(it.java))
        }
    }

    fun build(): Family {
        val allBitSet = BitSet().apply {
            allOf.forEach { this.set(it.id) }
        }
        val noneBitSet = BitSet().apply {
            noneOf.forEach { this.set(it.id) }
        }
        val anyBitSet = BitSet().apply {
            anyOf.forEach { this.set(it.id) }
        }
        val family = Family(world, allBitSet, noneBitSet, anyBitSet)
        if (families.contains(family)) {
            return families.get(family)
        }
        allOf.forEach { it.addListener(family) }
        noneOf.forEach { it.addListener(family) }
        anyOf.forEach { it.addListener(family) }
        families.add(family)
        return family
    }
}

data class Family(
    private val world: World,
    private val allOf: BitSet,
    private val noneOf: BitSet,
    private val anyOf: BitSet,
    private val checkAll: Boolean = allOf.cardinality() > 0,
    private val checkNone: Boolean = noneOf.cardinality() > 0,
    private val checkAny: Boolean = anyOf.cardinality() > 0
) : ComponentListener {
    val entities = BitSet(world.initialEntityCapacity)

    private fun updateFamilyEntities(entityID: Int) {
        val components = world.components(entityID)

        if (checkAll) {
            var idx = allOf.nextSetBit(0)
            while (idx >= 0) {
                if (!components[idx]) {
                    entities.clear(entityID)
                    return
                }

                if (idx == Integer.MAX_VALUE) {
                    break // or (idx+1) would overflow
                }
                idx = allOf.nextSetBit(idx + 1)
            }
        }

        if (checkNone && noneOf.intersects(components)) {
            entities.clear(entityID)
            return
        }

        if (checkAny && !anyOf.intersects(components)) {
            entities.clear(entityID)
            return
        }

        entities.set(entityID)
    }

    override fun componentAdded(entityID: Int, manager: ComponentManager<*>) = updateFamilyEntities(entityID)

    override fun componentRemoved(entityID: Int, manager: ComponentManager<*>) = updateFamilyEntities(entityID)

    inline fun iterate(action: (Int) -> Unit) {
        var entityID = entities.nextSetBit(0)
        while (entityID >= 0) {
            action(entityID)

            if (entityID == Integer.MAX_VALUE) {
                break // or (entityID+1) would overflow
            }
            entityID = entities.nextSetBit(entityID + 1)
        }
    }
}
