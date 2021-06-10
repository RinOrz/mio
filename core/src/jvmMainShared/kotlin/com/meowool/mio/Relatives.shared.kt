@file:Suppress("NewApi")

package com.meowool.mio

import java.nio.file.LinkOption
import kotlin.io.path.relativeTo

/**
 * Returns the real path of an existing file.
 *
 * @see java.nio.file.Path.toRealPath for more details
 *
 * @author 凛 (https://github.com/RinOrz)
 */
actual fun Path.toRealPath(followLinks: Boolean): Path = runCatching {
  when {
    followLinks -> this.toNioPath().toRealPath()
    else -> this.toNioPath().toRealPath(LinkOption.NOFOLLOW_LINKS)
  }.toMioPath()
}.getOrElse {
  this.toFile().canonicalFile.toMioPath()
}

/**
 * Adds [relative] name to this, considering this as a directory.
 * If [relative] has a root, [relative] is returned back.
 *
 * For example:
 * `Path("/foo/bar").resolve("gav")` is `/foo/bar/gav`
 * `Path("/foo/bar").resolve("/gav")` is `/gav`
 *
 * @return concatenated this and [relative] paths, or just [relative] if it's absolute.
 * @see java.nio.file.Path.resolve for more details
 */
actual fun Path.resolve(relative: String): Path = runCatching {
  this.toNioPath().resolve(relative).toMioPath()
}.getOrElse {
  this.toFile().resolve(relative).toMioPath()
}

/**
 * Adds [relative] path to this, considering this as a directory.
 * If [relative] has a root, [relative] is returned back.
 *
 * For example:
 * `Path("/foo/bar").resolve(Paths.get("gav"))` is `/foo/bar/gav`
 * `Path("/foo/bar").resolve(Paths.get("/gav"))` is `/gav`
 *
 * @return concatenated this and [relative] paths, or just [relative] if it's absolute.
 * @see java.nio.file.Path.resolve for more details
 */
actual fun Path.resolve(relative: Path): Path = runCatching {
  this.toNioPath().resolve(relative.toNioPath()).toMioPath()
}.getOrElse {
  this.toFile().resolve(relative.toFile()).toMioPath()
}

/**
 * Adds [relative] path to this, considering this as a directory.
 * If [relative] has a root, [relative] is returned back.
 *
 * For example:
 * `Path("/foo/bar").resolve(Paths.get("gav"))` is `/foo/bar/gav`
 * `Path("/foo/bar").resolve(Paths.get("/gav"))` is `/gav`
 *
 * @return concatenated this and [relative] paths, or just [relative] if it's absolute.
 * @see java.nio.file.Path.resolve for more details
 */
fun Path.resolve(relative: NioPath): Path = this.toNioPath().resolve(relative).toMioPath()

/**
 * Adds [relative] name to this parent directory.
 * If [relative] has a root or this has no parent directory, [relative] is returned back.
 *
 * For example:
 * `Path("/foo/bar").resolveSibling("gav")` is `/foo/gav`
 * `Path("/foo/bar").resolveSibling("/gav")` is `/gav`
 *
 * @return concatenated this.parent and [relative] paths, or just [relative] if it's absolute or this has no parent.
 * @see java.nio.file.Path.resolveSibling for more details
 */
actual fun Path.resolveSibling(relative: String): Path = runCatching {
  this.toNioPath().resolveSibling(relative).toMioPath()
}.getOrElse {
  this.toFile().resolveSibling(relative).toMioPath()
}

/**
 * Adds [relative] path to this parent directory.
 * If [relative] has a root or this has no parent directory, [relative] is returned back.
 *
 * For example:
 * `Path("/foo/bar").resolveSibling(Paths.get("gav"))` is `/foo/gav`
 * `Path("/foo/bar").resolveSibling(Paths.get("/gav"))` is `/gav`
 *
 * @return concatenated this.parent and [relative] paths, or just [relative] if it's absolute or this has no parent.
 * @see java.nio.file.Path.resolveSibling for more details
 */
actual fun Path.resolveSibling(relative: Path): Path = runCatching {
  this.toNioPath().resolveSibling(relative.toNioPath()).toMioPath()
}.getOrElse {
  this.toFile().resolveSibling(relative.toFile()).toMioPath()
}

/**
 * Adds [relative] path to this parent directory.
 * If [relative] has a root or this has no parent directory, [relative] is returned back.
 *
 * For example:
 * `Path("/foo/bar").resolveSibling(Paths.get("gav"))` is `/foo/gav`
 * `Path("/foo/bar").resolveSibling(Paths.get("/gav"))` is `/gav`
 *
 * @return concatenated this.parent and [relative] paths, or just [relative] if it's absolute or this has no parent.
 * @see java.nio.file.Path.resolveSibling for more details
 */
fun Path.resolveSibling(relative: NioPath): Path = this.toNioPath().resolveSibling(relative).toMioPath()

/**
 * Constructs a relative path between this path and a given [base] path.
 *
 * @see java.nio.file.Path.relativize for more details
 */
actual fun Path.relativeTo(base: Path): Path = runCatching {
  this.toNioPath().relativize(base.toNioPath()).toMioPath()
}.getOrElse {
  this.toFile().relativeTo(base.toFile()).toMioPath()
}

/**
 * Calculates the relative path for this path from a [base] path.
 *
 * Note that the [base] path is treated as a directory.
 * If this path matches the [base] path, then a [Path] with an empty path will be returned.
 *
 * @return the relative path from [base] to this.
 *
 * @throws IllegalArgumentException if this and base paths have different roots.
 */
fun Path.relativeTo(base: NioPath): Path = this.toNioPath().relativeTo(base).toMioPath()