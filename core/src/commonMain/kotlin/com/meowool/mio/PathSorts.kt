@file:Suppress("NOTHING_TO_INLINE")

package com.meowool.mio

/**
 * Used to [IPath] lists sorting.
 *
 * @see sorted
 * @see sortedBy
 */
interface PathSortStrategy {

  /**
   * Add a default comparison rule of path-sites.
   *
   * @see IPath.compareTo
   */
  fun default()

  /**
   * Add the comparison rule of the files/directories name.
   *
   * @param ignoreCase whether the names sort result is case-sensitive.
   *
   * @see IPath.name
   */
  fun name(ignoreCase: Boolean = true)

  /**
   * Add the comparison rule of the files/directories size.
   *
   * @param recursively when the compare to directory, whether to compare all its children.
   *
   * @see IPath.name
   */
  fun size(recursively: Boolean = false)

  /**
   * Add the comparison rule of path-sites type.
   *
   * @param ignoreCase whether the extensions sort result is case-sensitive.
   *
   * @see IFile.extension
   */
  fun extension(ignoreCase: Boolean)

  /**
   * Add the comparison rule of the files/directories last modified time.
   *
   * @see IPath.lastModifiedTime
   */
  fun lastModified()

  /**
   * Add a comparison rule to allow files to be displayed prior to directories.
   *
   * @see IPath.isRegularFile
   */
  fun filesFirst()

  /**
   * Add a comparison rule to allow directories to be displayed prior to files.
   *
   * @see IPath.isDirectory
   */
  fun directoriesFirst()

  /**
   * Add the comparison rule to make hidden files or directories display first.
   *
   * @see IPath.isHidden
   */
  fun hiddenFirst()

  /**
   * Add a custom sorting rule.
   *
   * @param comparator how to compare two path-sites for sort.
   */
  fun custom(comparator: Comparator<IPath>)

  /**
   * Add reverse sorting rule.
   */
  fun reversed()

  /**
   * Returns the comparator about this strategy.
   */
  fun get(): Comparator<IPath>
}

/**
 * A default strategy for path sites sorting.
 */
@PublishedApi
internal class DefaultPathSortStrategy : PathSortStrategy {
  private var comparator = compareBy<IPath> { 0 }

  override fun default() {
    comparator = comparator.thenBy { it }
  }

  override fun name(ignoreCase: Boolean) {
    comparator = comparator.thenComparator { o1, o2 ->
      o1.name.compareTo(o2.name, ignoreCase)
    }
  }

  override fun size(recursively: Boolean) {
    comparator = compareBy<IPath> {
      if (recursively && it is Directory) it.totalSize else it.size
    }.then(comparator)
  }

  override fun extension(ignoreCase: Boolean) {
    comparator = Comparator<IPath> { o1, o2 ->
      if (o1 is IFile && o2 is IFile) o1.extension.compareTo(o2.extension, ignoreCase) else 0
    }.then(comparator)
  }

  override fun lastModified() {
    comparator = compareBy<IPath> { it.lastModifiedTime }.then(comparator)
  }

  override fun filesFirst() {
    comparator = compareByDescending<IPath> { it.isRegularFile }.then(comparator)
  }

  override fun directoriesFirst() {
    comparator = compareByDescending<IPath> { it.isDirectory }.then(comparator)
  }

  override fun hiddenFirst() {
    comparator = compareByDescending<IPath> { it.isHidden }.then(comparator)
  }

  override fun custom(comparator: Comparator<IPath>) {
    this.comparator = this.comparator.then(comparator)
  }

  override fun reversed() {
    comparator = comparator.reversed()
  }

  override fun get(): Comparator<IPath> = comparator
}


/**
 * According by a fluent DSL [declaration] to sort this [Iterable] of path-sites.
 *
 * @param strategy the instance of sort strategy.
 * @param declaration used to declare the [strategy].
 */
inline fun Iterable<IPath>.sortedBy(
  strategy: PathSortStrategy = DefaultPathSortStrategy(),
  declaration: PathSortStrategy.() -> Unit,
) = this.sortedWith(strategy.apply(declaration))

/**
 * Sort this [Iterable] of path-sites by [strategy].
 */
fun Iterable<IPath>.sortedWith(strategy: PathSortStrategy) = this.sortedWith(strategy.get())

/**
 * Sort this [Iterable] of path-sites using the default strategy.
 *
 * @see DefaultPathSortStrategy
 */
fun Iterable<IPath>.sorted() = this.sortedWith(DefaultPathSortStrategy())

/**
 * According by a fluent DSL [declaration] to sort this [Array] of path-sites.
 *
 * @param strategy the instance of sort strategy.
 * @param declaration used to declare the [strategy].
 */
fun Array<IPath>.sortedBy(
  strategy: PathSortStrategy = DefaultPathSortStrategy(),
  declaration: PathSortStrategy.() -> Unit,
) = this.sortedWith(strategy.apply(declaration))

/**
 * Sort this [Array] of path-sites by [strategy].
 */
fun Array<IPath>.sortedWith(strategy: PathSortStrategy) = this.sortedWith(strategy.get())

/**
 * Sort this [Array] of path-sites using the default strategy.
 *
 * @see DefaultPathSortStrategy
 */
fun Array<IPath>.sorted() = this.sortedWith(DefaultPathSortStrategy())

/**
 * According by a fluent DSL [declaration] to sort this [Sequence] of path-sites.
 *
 * @param strategy the instance of sort strategy.
 * @param declaration used to declare the [strategy].
 */
fun Sequence<IPath>.sortedBy(
  strategy: PathSortStrategy = DefaultPathSortStrategy(),
  declaration: PathSortStrategy.() -> Unit,
) = this.sortedWith(strategy.apply(declaration))

/**
 * Sort this [Sequence] of path-sites by [strategy].
 */
fun Sequence<IPath>.sortedWith(strategy: PathSortStrategy) = this.sortedWith(strategy.get())

/**
 * Sort this [Sequence] of path-sites using the default strategy.
 *
 * @see DefaultPathSortStrategy
 */
fun Sequence<IPath>.sorted() = this.sortedWith(DefaultPathSortStrategy())
