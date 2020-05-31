package com.github.quillraven.kecs

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.kecs.component.TransformComponent
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@Suppress("UNUSED")
object KECSEntitySpec : Spek({
    describe("An entity") {
        val manager by memoized { KECSManager() }
        val entity by memoized { manager.entity() }

        describe("adding a component") {
            beforeEachTest {
                entity.add<TransformComponent> {
                    position.set(1f, 1f)
                }
            }

            it("should contain component") {
                (TransformComponent::class in entity.components) `should be equal to` true
            }

            it("should have properties of init block") {
                entity.get<TransformComponent>().position `should be equal to` Vector2(1f, 1f)
            }
        }

        describe("removing an existing component") {
            beforeEachTest {
                entity.run {
                    add<TransformComponent>()
                    remove<TransformComponent>()
                }
            }

            it("should not contain component") {
                (TransformComponent::class in entity.components) `should be equal to` false
            }

            it("component should be returned to component pool") {
                manager.freeComponentSize(TransformComponent::class) `should be equal to` 1
            }
        }

        describe("adding an already existing component") {
            beforeEachTest {
                entity.add<TransformComponent>()
            }

            it("should throw a KECSComponentAlreadyExistingException") {
                invoking { entity.add<TransformComponent>() } `should throw` KECSComponentAlreadyExistingException::class
            }
        }

        describe("removing a non-existing component") {
            it("should throw a KECSMissingComponentException") {
                invoking { entity.remove<TransformComponent>() } `should throw` KECSMissingComponentException::class
            }
        }

        describe("comparing with other entities") {
            lateinit var entity1: KECSEntity
            lateinit var entity2: KECSEntity
            lateinit var entity3: KECSEntity
            beforeEachTest {
                entity1 = manager.entity {
                    add<TransformComponent> {
                        position.set(1f, 1f)
                    }
                }
                entity2 = manager.entity {
                    add<TransformComponent> {
                        position.set(0f, 0f)
                    }
                }
                entity3 = manager.entity {
                    add<TransformComponent> {
                        position.set(1f, 1f)
                    }
                }
            }

            it("should return true when comparing against itself") {
                (entity1 == entity1) `should be equal to` true
            }

            it("should return false when comparing against a different entity") {
                (entity1 == entity2) `should be equal to` false
            }

            it("should return false when comparing against a different entity with the same component data") {
                (entity1 == entity3) `should be equal to` false
            }
        }
    }
})
