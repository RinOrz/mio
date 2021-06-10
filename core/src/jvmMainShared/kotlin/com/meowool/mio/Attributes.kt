@file:Suppress("NewApi")

package com.meowool.mio

import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttributeView

/**
 * Returns a basic file attribute view.
 *
 * @see Path.getAttributeView
 */
fun Path.getBasicAttributeView(): BasicFileAttributeView = getAttributeView()

/**
 * Returns a file attribute view of a given type.
 *
 * @receiver path the path to the file
 * @param V the [BasicFileAttributes] type object corresponding to the file attribute view
 * @param options options indicating how symbolic links are handled
 * @return a file attribute view of the specified type, or null if the attribute view type is
 * not available
 *
 * @see Files.getFileAttributeView
 */
@Throws(IOException::class)
inline fun <reified V : FileAttributeView?> Path.getAttributeView(vararg options: LinkOption): V =
  Files.getFileAttributeView(this, V::class.java, *options)

/**
 * Reads a file's basic attributes as a bulk operation.
 *
 * @see Path.readAttributes
 */
fun Path.readBasicAttributes(): BasicFileAttributes = readAttributes()

/**
 * Reads a file's attributes as a bulk operation.
 *
 * @receiver path the path to the file
 * @param A the [BasicFileAttributes] type of the file attributes required to read
 * @param options options indicating how symbolic links are handled
 * @return the file attributes
 *
 * @see Files.readAttributes
 */
@Throws(IOException::class)
inline fun <reified A : BasicFileAttributes> Path.readAttributes(vararg options: LinkOption): A =
  Files.readAttributes(this, A::class.java, *options)
