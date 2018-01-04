package com.ue.fingercoloring.util

import java.util.*

class SizedStack<Any>(private val maxSize: Int) : Stack<Any>() {

    override fun push(obj: Any): Any {
        //If the stack is too big, remove elements until it's the right size.
        while (this.size >= maxSize) {
            this.removeAt(0)
        }
        return super.push(obj)
    }

}