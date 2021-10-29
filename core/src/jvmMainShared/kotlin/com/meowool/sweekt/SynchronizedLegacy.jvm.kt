package com.meowool.sweekt

actual inline fun <R> Any.synchronized(lock: Any, block: () -> R): R =
  kotlin.synchronized(lock, block)