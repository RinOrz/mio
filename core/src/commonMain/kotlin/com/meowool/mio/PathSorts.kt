@file:Suppress("NOTHING_TO_INLINE")

package com.meowool.mio

/**
 * Used to [Path] lists sorting.
 *
 * @see sorted
 * @see sortedBy
 */
interface PathSortStrategy {

  /**
   * Add a default comparison rule of path-sites.
   *
   * @see Path.compareTo
   */
  fun default()

  /**
   * Add the comparison rule of the files/directories name.
   *
   * @param ignoreCase whether the names sort result is case-sensitive.
   *
   * @see Path.name
   */
  fun name(ignoreCase: Boolean = true)

  /**
   * Add the comparison rule of the files/directories size.
   *
   * @param recursively when the compare to directory, whether to compare all its children.
   *
   * @see Path.name
   */
  fun size(recursively: Boolean = false)

  /**
   * Add the comparison rule of path-sites type.
   *
   * @param ignoreCase whether the extensions sort result is case-sensitive.
   *
   * @see Path.extension
   */
  fun extension(ignoreCase: Boolean)

  /**
   * Add the comparison rule of the files/directories last modified time.
   *
   * @see Path.lastModifiedTime
   */
  fun lastModified()

  /**
   * Add a comparison rule to allow files to be displayed prior to directories.
   *
   * @see Path.isRegularFile
   */
  fun filesFirst()

  /**
   * Add a comparison rule to allow directories to be displayed prior to files.
   *
   * @see Path.isDirectory
   */
  fun directoriesFirst()

  /**
   * Add the comparison rule to make hidden files or directories display first.
   *
   * @see Path.isHidden
   */
  fun hiddenFirst()

  /**
   * Add a custom sorting rule.
   *
   * @param comparator how to compare two path-sites for sort.
   */
  fun custom(comparator: Comparator<Path>)

  /**
   * Add reverse sorting rule.
   */
  fun reversed()

  /**
   * Returns the comparator about this strategy.
   */
  fun get(): Comparator<Path>
}

/**
 * A default strategy for path sites sorting.
 */
@PublishedApi
internal class DefaultPathSortStrategy : PathSortStrategy {
  private var comparator = compareBy<Path> { 0 }

  override fun default() {
    comparator = comparator.thenBy { it }
  }

  override fun name(ignoreCase: Boolean) {
    comparator = comparator.thenComparator { o1, o2 ->
      o1.name.compareTo(o2.name, ignoreCase)
    }
  }

  override fun size(recursively: Boolean) {
    comparator = compareBy<Path> { if (recursively) it.totalSize else it.size }.then(comparator)
  }

  override fun extension(ignoreCase: Boolean) {
    comparator = Comparator<Path> { o1, o2 ->
      o1.extension.compareTo(o2.extension, ignoreCase)
    }.then(comparator)
  }

  override fun lastModified() {
    comparator = compareBy<Path> { it.lastModifiedTime }.then(comparator)
  }

  override fun filesFirst() {
    comparator = compareByDescending<Path> { it.isRegularFile }.then(comparator)
  }

  override fun directoriesFirst() {
    comparator = compareByDescending<Path> { it.isDirectory }.then(comparator)
  }

  override fun hiddenFirst() {
    comparator = compareByDescending<Path> { it.isHidden }.then(comparator)
  }

  override fun custom(comparator: Comparator<Path>) {
    this.comparator = this.comparator.then(comparator)
  }

  override fun reversed() {
    comparator = comparator.reversed()
  }

  override fun get(): Comparator<Path> = comparator
}


/**
 * According by a fluent DSL [declaration] to sort this [Iterable] of path-sites.
 *
 * @param strategy the instance of sort strategy.
 * @param declaration used to declare the [strategy].
 */
inline fun Iterable<Path>.sortedBy(
  strategy: PathSortStrategy = DefaultPathSortStrategy(),
  declaration: PathSortStrategy.() -> Unit,
) = this.sortedWith(strategy.apply(declaration))

/**
 * Sort this [Iterable] of path-sites by [strategy].
 */
fun Iterable<Path>.sortedWith(strategy: PathSortStrategy) = this.sortedWith(strategy.get())

/**
 * Sort this [Iterable] of path-sites using the default strategy.
 *
 * @see DefaultPathSortStrategy
 */
fun Iterable<Path>.sorted() = this.sortedWith(DefaultPathSortStrategy())

/**
 * According by a fluent DSL [declaration] to sort this [Array] of path-sites.
 *
 * @param strategy the instance of sort strategy.
 * @param declaration used to declare the [strategy].
 */
fun Array<Path>.sortedBy(
  strategy: PathSortStrategy = DefaultPathSortStrategy(),
  declaration: PathSortStrategy.() -> Unit,
) = this.sortedWith(strategy.apply(declaration))

/**
 * Sort this [Array] of path-sites by [strategy].
 */
fun Array<Path>.sortedWith(strategy: PathSortStrategy) = this.sortedWith(strategy.get())

/**
 * Sort this [Array] of path-sites using the default strategy.
 *
 * @see DefaultPathSortStrategy
 */
fun Array<Path>.sorted() = this.sortedWith(DefaultPathSortStrategy())

/**
 * According by a fluent DSL [declaration] to sort this [Sequence] of path-sites.
 *
 * @param strategy the instance of sort strategy.
 * @param declaration used to declare the [strategy].
 */
fun Sequence<Path>.sortedBy(
  strategy: PathSortStrategy = DefaultPathSortStrategy(),
  declaration: PathSortStrategy.() -> Unit,
) = this.sortedWith(strategy.apply(declaration))

/**
 * Sort this [Sequence] of path-sites by [strategy].
 */
fun Sequence<Path>.sortedWith(strategy: PathSortStrategy) = this.sortedWith(strategy.get())

/**
 * Sort this [Sequence] of path-sites using the default strategy.
 *
 * @see DefaultPathSortStrategy
 */
fun Sequence<Path>.sorted() = this.sortedWith(DefaultPathSortStrategy())
