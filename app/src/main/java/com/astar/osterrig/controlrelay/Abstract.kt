package com.astar.osterrig.controlrelay

abstract class Abstract {

    abstract class Object<T, M: Mapper> {
        abstract fun map(mapper: T): M
    }

    interface Mapper
}