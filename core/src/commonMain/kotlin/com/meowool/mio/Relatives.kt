package com.meowool.mio

/**
 * Returns the real path of an existing file.
 *
 * @param followLinks options indicating how symbolic links are handled.
 * @author 凛 (https://github.com/RinOrz)
 */
expect fun Path.toRealPath(followLinks: Boolean = true): Path

/**
 * Adds [relative] name to this, considering this as a directory.
 * If [relative] has a root, [relative] is returned back.
 *
 * For example:
 * `Path("/foo/bar").resolve("gav")` is `/foo/bar/gav`
 * `Path("/foo/bar").resolve("/gav")` is `/gav`
 *
 * @return concatenated this and [relative] paths, or just [relative] if it's absolute.
 */
expect fun Path.resolve(relative: String): Path

/**
 * Adds [relative] path to this, considering this as a directory.
 * If [relative] has a root, [relative] is returned back.
 *
 * For example:
 * `Path("/foo/bar").resolve(Paths.get("gav"))` is `/foo/bar/gav`
 * `Path("/foo/bar").resolve(Paths.get("/gav"))` is `/gav`
 *
 * @return concatenated this and [relative] paths, or just [relative] if it's absolute.
 */
expect fun Path.resolve(relative: Path): Path

/**
 * Adds [relative] name to this parent directory.
 * If [relative] has a root or this has no parent directory, [relative] is returned back.
 *
 * For example:
 * `Path("/foo/bar").resolveSibling("gav")` is `/foo/gav`
 * `Path("/foo/bar").resolveSibling("/gav")` is `/gav`
 *
 * @return concatenated this.parent and [relative] paths, or just [relative] if it's absolute or this has no parent.
 */
expect fun Path.resolveSibling(relative: String): Path

/**
 * Adds [relative] path to this parent directory.
 * If [relative] has a root or this has no parent directory, [relative] is returned back.
 *
 * For example:
 * `Path("/foo/bar").resolveSibling(Paths.get("gav"))` is `/foo/gav`
 * `Path("/foo/bar").resolveSibling(Paths.get("/gav"))` is `/gav`
 *
 * @return concatenated this.parent and [relative] paths, or just [relative] if it's absolute or
 * this has no parent.
 */
expect fun Path.resolveSibling(relative: Path): Path

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
expect fun Path.relativeTo(base: Path): Path