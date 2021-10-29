package com.meowool.mio

/**
 * Channel is empty exception.
 */
open class ChannelEmptyException(message: String? = null) : Exception(message)

/**
 * Channel data size underflow exception.
 */
open class ChannelUnderflowException(message: String? = null) : Exception(message)

/**
 * Channel data size overflow exception.
 */
open class ChannelOverflowException(message: String? = null) : Exception(message)