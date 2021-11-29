package com.meowool.io.internal

inline fun <T> T.ifTrue(condition: (T) -> Boolean, another: (T) -> T) =
  if (condition(this)) this else another(this)