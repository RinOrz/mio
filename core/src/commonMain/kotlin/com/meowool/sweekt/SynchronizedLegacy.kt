package com.meowool.sweekt

expect inline fun <R> Any.synchronized(lock: Any = this, block: () -> R): R