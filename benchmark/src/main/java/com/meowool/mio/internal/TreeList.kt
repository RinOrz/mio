@file:Suppress("NOTHING_TO_INLINE", "NAME_SHADOWING")

package com.meowool.mio.internal

import kotlin.math.max

/**
 * A big list using AVL-Tree structure.
 *
 * Modified from [Apache commons-collection](https://github.com/apache/commons-collections/blob/master/src/main/java/org/apache/commons/collections4/list/TreeList.java).
 *
 * @author 凛 (https://github.com/RinOrz)
 */
internal class TreeList<E> {
  /** The root node in the AVL tree  */
  private var root: AVLNode<E>? = null
  private var last: E? = null

  /** The current size of this list. */
  var size: Long = 0

  /** Returns the last index of this list. */
  val lastIndex: Long inline get() = size - 1

  inline fun first(): E = firstOrNull()!!

  inline fun last(): E = lastOrNull()!!

  inline fun firstOrNull(): E? = get(0)

  inline fun lastOrNull(): E? = get(lastIndex)

  /**
   * Returns the element at the specified [index].
   *
   * @param index the index to retrieve
   * @return the element at the specified index, or `null` if this tree is empty
   */
  inline operator fun get(index: Long): E {
    if (index == lastIndex && last != null) return last!!
    checkInterval(index, lastIndex)
    val node = root?.get(index) ?: throw NoSuchElementException("index = $index")
    @Suppress("UNCHECKED_CAST")
    return node.value as E
  }

  /**
   * Returns the element at the specified [index].
   *
   * @param index the index to retrieve
   * @return the element at the specified index, or `null` if this tree is empty
   */
  fun getOrNull(index: Long): E? {
    if (index == lastIndex) return last
    if (index < 0 || index > lastIndex) return null
    return root?.get(index)?.value
  }

  /**
   * Adds a new element to the specified [index] of this list.
   *
   * @param index the index to add before
   * @param element the element to add
   */
  fun addAt(index: Long, element: E) {
    checkInterval(index, size++)
    root = root?.insert(index, element) ?: AVLNode(index, element, null, null)
    if (index == size) last = element
  }

  /**
   * Adds a new element to the end of this list.
   *
   * @param element the element to add
   */
  inline fun add(element: E) = addAt(size, element)

  operator fun plusAssign(element: E) = add(element)

  /**
   * Appends all the elements in the specified collection to the end of this list,
   * in the order that they are returned by the specified collection's Iterator.
   *
   *
   * This method runs in O(n + log m) time, where m is
   * the size of this list and n is the size of `c`.
   *
   * @param c  the collection to be added to this list
   * @return `true` if this list changed as a result of the call
   * @throws NullPointerException {@inheritDoc}
   */
  fun addAll(c: Collection<E?>): Boolean {
    if (c.isEmpty()) {
      return false
    }
    val cTree: AVLNode<E> = AVLNode(c)
    root = root?.addAll(cTree, size) ?: cTree
    size += c.size.toLong()
    return true
  }

  /**
   * Sets the element at the specified index.
   *
   * @param index the index to set
   * @param element the element to store at the specified index
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  operator fun set(index: Long, element: E) {
    checkInterval(index, lastIndex)
    if (root == null) root = AVLNode(size, element, null, null)
    root!![index]?.apply { value = element } ?: run { addAt(index, element) }
  }

  /**
   * Removes the element at the specified index.
   *
   * @param index the index to remove
   */
  fun removeAt(index: Long) {
    checkInterval(index, lastIndex)
    root?.run {
      root = remove(index)
      size--
    }
  }

  /**
   * Clears the list, removing all entries.
   */
  fun clear() {
    root = null
    size = 0
  }

  /** Similar to the [List.forEach] */
  inline fun forEach(action: (E) -> Unit) = forEachIndexed { _, element -> action(element) }

  /** Similar to the [List.forEachIndexed] */
  inline fun forEachIndexed(action: (index: Long, element: E) -> Unit) {
    val root = root ?: return
    var nextIndex = 0L
    var next = root[0]
    while (nextIndex < size) {
      if (next == null) next = root[nextIndex]
      action(nextIndex, next!!.value!!)
      nextIndex++
      next = next.next()
    }
  }

  /**
   * Checks whether the index is valid.
   *
   * @param index the index to check
   * @param endIndex the last allowed index
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  @Throws(IndexOutOfBoundsException::class)
  private inline fun checkInterval(index: Long, endIndex: Long) {
    if (index < 0 || index > endIndex) {
      throw IndexOutOfBoundsException("Invalid index=$index, size=$size")
    }
  }

  internal class AVLNode<E> {

    /** The stored element.  */
    var value: E? = null

    /** The left child node or the predecessor if [.leftIsPrevious]. */
    private var left: AVLNode<E>? = null

    /** Flag indicating that left reference is not a subtree but the predecessor.  */
    private var leftIsPrevious = false

    /** The right child node or the successor if [.rightIsNext].  */
    private var right: AVLNode<E>? = null

    /** Flag indicating that right reference is not a subtree but the successor.  */
    private var rightIsNext = false

    /** How many levels of left/right are below this one.  */
    private var height: Long = 0

    /** The relative position, root holds absolute position.  */
    private var relativePosition: Long = 0

    /** Left node. */
    private val leftSubTree: AVLNode<E>? get() = if (leftIsPrevious) null else left

    /** Right node. */
    private val rightSubTree: AVLNode<E>? get() = if (rightIsNext) null else right

    /**
     * Constructs a new node with a relative position.
     *
     * @param relativePosition the relative position of the node
     * @param value the value for the node
     * @param rightFollower the node with the value following this one
     * @param leftFollower the node with the value leading this one
     */
    constructor(
      relativePosition: Long,
      value: E,
      rightFollower: AVLNode<E>?,
      leftFollower: AVLNode<E>?,
    ) {
      this.relativePosition = relativePosition
      this.value = value
      this.rightIsNext = true
      this.leftIsPrevious = true
      this.right = rightFollower
      this.left = leftFollower
    }

    /**
     * Constructs a new AVL tree from a collection.
     *
     *
     * The collection must be nonempty.
     *
     * @param coll  a nonempty collection
     */
    constructor(coll: Collection<E?>) : this(coll.iterator(), 0, coll.size - 1L, 0, null, null)

    /**
     * Constructs a new AVL tree from a collection.
     *
     *
     * This is a recursive helper for [.AVLNode]. A call
     * to this method will construct the subtree for elements `start`
     * through `end` of the collection, assuming the iterator
     * `e` already points at element `start`.
     *
     * @param iterator an iterator over the collection, which should already point
     * to the element at index `start` within the collection
     * @param start the index of the first element in the collection that
     * should be in this subtree
     * @param end the index of the last element in the collection that
     * should be in this subtree
     * @param absolutePositionOfParent absolute position of this node's
     * parent, or 0 if this node is the root
     * @param prev the `AVLNode` corresponding to element (start - 1)
     * of the collection, or null if start is 0
     * @param next the `AVLNode` corresponding to element (end + 1)
     * of the collection, or null if end is the last element of the collection
     */
    constructor(
      iterator: Iterator<E?>,
      start: Long,
      end: Long,
      absolutePositionOfParent: Long,
      prev: AVLNode<E>?,
      next: AVLNode<E>?,
    ) {
      val mid = start + (end - start) / 2
      if (start < mid) {
        left = AVLNode(iterator, start, mid - 1, mid, prev, this)
      } else {
        leftIsPrevious = true
        left = prev
      }
      value = iterator.next()
      relativePosition = mid - absolutePositionOfParent
      if (mid < end) {
        right = AVLNode(iterator, mid + 1, end, mid, this, next)
      } else {
        rightIsNext = true
        right = next
      }
      recalculateHeight()
    }

    /**
     * Locate the element with the given index relative to the
     * offset of the parent of this node.
     */
    operator fun get(index: Long): AVLNode<E>? {
      val indexRelativeToMe = index - relativePosition
      if (indexRelativeToMe == 0L) return this
      val nextNode = (if (indexRelativeToMe < 0) leftSubTree else rightSubTree) ?: return null
      return nextNode[indexRelativeToMe]
    }

    /**
     * Gets the next node in the list after this one.
     *
     * @return the next node
     */
    fun next(): AVLNode<E>? = if (rightIsNext || right == null) right else right?.min()

    /**
     * Inserts a node at the position index.
     *
     * @param index is the index of the position relative to the position of
     * the parent node.
     * @param obj is the object to be stored in the position.
     */
    fun insert(index: Long, obj: E): AVLNode<E> {
      val indexRelativeToMe = index - relativePosition
      return if (indexRelativeToMe <= 0) {
        insertOnLeft(indexRelativeToMe, obj)
      } else {
        insertOnRight(indexRelativeToMe, obj)
      }
    }

    /**
     * Removes the node at a given position.
     *
     * @param index is the index of the element to be removed relative to the position of
     * the parent node of the current node.
     */
    fun remove(index: Long): AVLNode<E>? {
      val indexRelativeToMe = index - relativePosition
      if (indexRelativeToMe == 0L) return removeSelf()
      if (indexRelativeToMe > 0) {
        setRight(right!!.remove(indexRelativeToMe), right!!.right)
        if (relativePosition < 0) relativePosition++
      } else {
        setLeft(left!!.remove(indexRelativeToMe), left!!.left)
        if (relativePosition > 0) relativePosition--
      }
      recalculateHeight()
      return balance()
    }

    /**
     * Appends the elements of another tree list to this tree list by efficiently
     * merging the two AVL trees. This operation is destructive to both trees and
     * runs in O(log(m + n)) time.
     *
     * @param otherTree
     * the root of the AVL tree to merge with this one
     * @param currentSize
     * the number of elements in this AVL tree
     * @return the root of the new, merged AVL tree
     */
    fun addAll(otherTree: AVLNode<E>, currentSize: Long): AVLNode<E>? {
      var otherTree: AVLNode<E>? = otherTree
      val maxNode = max()
      val otherTreeMin = otherTree!!.min()

      // We need to efficiently merge the two AVL trees while keeping them
      // balanced (or nearly balanced). To do this, we take the shorter
      // tree and combine it with a similar-height subtree of the taller
      // tree. There are two symmetric cases:
      //   * this tree is taller, or
      //   * otherTree is taller.
      if (otherTree.height > height) {
        // CASE 1: The other tree is taller than this one. We will thus
        // merge this tree into otherTree.

        // STEP 1: Remove the maximum element from this tree.
        val leftSubTree = removeMax()

        // STEP 2: Navigate left from the root of otherTree until we
        // find a subtree, s, that is no taller than me. (While we are
        // navigating left, we store the nodes we encounter in a stack
        // so that we can re-balance them in step 4.)
        val sAncestors = ArrayDeque<AVLNode<E>>()
        var s = otherTree
        var sAbsolutePosition = (s.relativePosition + currentSize).toInt()
        var sParentAbsolutePosition = 0
        while (s != null && s.height > leftSubTree.height) {
          sParentAbsolutePosition = sAbsolutePosition
          sAncestors.addFirst(s)
          s = s.left
          if (s != null) {
            sAbsolutePosition += s.relativePosition.toInt()
          }
        }

        // STEP 3: Replace s with a newly constructed subtree whose root
        // is maxNode, whose left subtree is leftSubTree, and whose right
        // subtree is s.
        maxNode.setLeft(leftSubTree, null)
        maxNode.setRight(s, otherTreeMin)
        if (leftSubTree != null) {
          leftSubTree.max().setRight(null, maxNode)
          leftSubTree.relativePosition -= currentSize - 1
        }
        if (s != null) {
          s.min().setLeft(null, maxNode)
          s.relativePosition = sAbsolutePosition - currentSize + 1
        }
        maxNode.relativePosition = currentSize - 1 - sParentAbsolutePosition
        otherTree.relativePosition += currentSize

        // STEP 4: Re-balance the tree and recalculate the heights of s's ancestors.
        s = maxNode
        while (!sAncestors.isEmpty()) {
          val sAncestor: AVLNode<E> = sAncestors.removeFirst()
          sAncestor.setLeft(s, null)
          s = sAncestor.balance()
        }
        return s
      }
      otherTree = otherTree.removeMin()
      val sAncestors = ArrayDeque<AVLNode<E>>()
      var s: AVLNode<E>? = this
      var sAbsolutePosition = s!!.relativePosition.toInt()
      var sParentAbsolutePosition = 0
      while (s != null && s.height > otherTree.height) {
        sParentAbsolutePosition = sAbsolutePosition
        sAncestors.addFirst(s)
        s = s.right
        if (s != null) {
          sAbsolutePosition += s.relativePosition.toInt()
        }
      }
      otherTreeMin.setRight(otherTree, null)
      otherTreeMin.setLeft(s, maxNode)
      if (otherTree != null) {
        otherTree.min().setLeft(null, otherTreeMin)
        otherTree.relativePosition++
      }
      if (s != null) {
        s.max().setRight(null, otherTreeMin)
        s.relativePosition = sAbsolutePosition - currentSize
      }
      otherTreeMin.relativePosition = currentSize - sParentAbsolutePosition
      s = otherTreeMin
      while (!sAncestors.isEmpty()) {
        val sAncestor: AVLNode<E> = sAncestors.removeFirst()
        sAncestor.setRight(s, null)
        s = sAncestor.balance()
      }
      return s
    }

    private fun insertOnLeft(indexRelativeToMe: Long, obj: E): AVLNode<E> {
      if (leftSubTree == null) {
        setLeft(AVLNode(-1, obj, this, left), null)
      } else {
        setLeft(left!!.insert(indexRelativeToMe, obj), null)
      }
      if (relativePosition >= 0) {
        relativePosition++
      }
      return balance().apply {
        recalculateHeight()
      }
    }

    private fun insertOnRight(indexRelativeToMe: Long, obj: E): AVLNode<E> {
      if (rightSubTree == null) {
        setRight(AVLNode(+1, obj, right, this), null)
      } else {
        setRight(right!!.insert(indexRelativeToMe, obj), null)
      }
      if (relativePosition < 0) {
        relativePosition--
      }
      return balance().apply {
        recalculateHeight()
      }
    }

    private fun removeMax(): AVLNode<E>? {
      if (rightSubTree == null) return removeSelf()
      setRight(right!!.removeMax(), right!!.right)
      if (relativePosition < 0) relativePosition++
      recalculateHeight()
      return balance()
    }

    private fun removeMin(): AVLNode<E>? {
      if (leftSubTree == null) return removeSelf()
      setLeft(left!!.removeMin(), left!!.left)
      if (relativePosition > 0) relativePosition--
      recalculateHeight()
      return balance()
    }

    /**
     * Removes this node from the tree.
     *
     * @return the node that replaces this one in the parent
     */
    private fun removeSelf(): AVLNode<E>? {
      if (rightSubTree == null && leftSubTree == null) {
        return null
      }

      if (rightSubTree == null) {
        if (relativePosition > 0) left!!.relativePosition += relativePosition
        return left!!.apply { max().setRight(null, right) }
      }

      if (leftSubTree == null) {
        right!!.relativePosition += relativePosition - if (relativePosition < 0) 0 else 1
        return right!!.apply { min().setLeft(null, left) }
      }

      if (heightRightMinusLeft() > 0) {
        // more on the right, so delete from the right
        val rightMin = right!!.min()
        value = rightMin.value
        if (leftIsPrevious) {
          left = rightMin.left
        }
        right = right!!.removeMin()
        if (relativePosition < 0) {
          relativePosition++
        }
      } else {
        // more on the left or equal, so delete from the left
        val leftMax = left!!.max()
        value = leftMax.value
        if (rightIsNext) {
          right = leftMax.right
        }
        val leftPrevious = left!!.left
        left = left!!.removeMax()
        if (left == null) {
          // special case where left that was deleted was a double link
          // only occurs when height difference is equal
          left = leftPrevious
          leftIsPrevious = true
        }
        if (relativePosition > 0) {
          relativePosition--
        }
      }
      recalculateHeight()
      return this
    }

    /**
     * Sets the left field to the node, or the previous node if that is null
     *
     * @param node the new left subtree node
     * @param previous the previous node in the linked list
     */
    private fun setLeft(node: AVLNode<E>?, previous: AVLNode<E>?) {
      leftIsPrevious = node == null
      left = if (leftIsPrevious) previous else node
      recalculateHeight()
    }

    /**
     * Sets the right field to the node, or the next node if that is null
     *
     * @param node the new left subtree node
     * @param next the next node in the linked list
     */
    private fun setRight(node: AVLNode<E>?, next: AVLNode<E>?) {
      rightIsNext = node == null
      right = if (rightIsNext) next else node
      recalculateHeight()
    }

    private fun rotateLeft(): AVLNode<E> {
      val newTop = right // can't be faedelung!
      val movedNode = rightSubTree!!.leftSubTree
      val newTopPosition = relativePosition + newTop.offset
      val myNewPosition = -newTop!!.relativePosition
      val movedPosition = newTop.offset + movedNode.offset
      setRight(movedNode, newTop)
      newTop.setLeft(this, null)
      newTop.offset = newTopPosition
      this.offset = myNewPosition
      movedNode.offset = movedPosition
      return newTop
    }

    private fun rotateRight(): AVLNode<E> {
      val newTop = left // can't be faedelung
      val movedNode = leftSubTree!!.rightSubTree
      val newTopPosition = relativePosition + newTop.offset
      val myNewPosition = -newTop!!.relativePosition
      val movedPosition = newTop.offset + movedNode.offset
      setLeft(movedNode, newTop)
      newTop.setRight(this, null)
      newTop.offset = newTopPosition
      this.offset = myNewPosition
      movedNode.offset = movedPosition
      return newTop
    }

    /**
     * Balances according to the AVL algorithm.
     */
    private fun balance(): AVLNode<E> = when (heightRightMinusLeft()) {
      1L, 0L, -1L -> this
      -2L -> {
        if (left.heightRightMinusLeft() > 0) {
          setLeft(left!!.rotateLeft(), null)
        }
        rotateRight()
      }
      2L -> {
        if (right.heightRightMinusLeft() < 0) {
          setRight(right!!.rotateRight(), null)
        }
        rotateLeft()
      }
      else -> throw RuntimeException("tree inconsistent!")
    }

    /**
     * Gets the rightmost child of this node.
     *
     * @return the rightmost child (the greatest index)
     */
    private fun max(): AVLNode<E> = if (rightSubTree == null) this else right!!.max()

    /**
     * Gets the leftmost child of this node.
     *
     * @return the leftmost child (the smallest index)
     */
    private fun min(): AVLNode<E> = if (leftSubTree == null) this else left!!.min()

    /**
     * Sets the height by calculation.
     */
    private fun recalculateHeight() {
      height = max(leftSubTree?.height ?: -1, rightSubTree?.height ?: -1) + 1
    }

    companion object {

      /**
       * Returns the height difference right - left
       */
      private fun <E> AVLNode<E>?.heightRightMinusLeft(): Long =
        this?.rightSubTree.height - this?.leftSubTree.height

      /**
       * Returns the height of this node or `-1` if the node is `null`.
       */
      private val <E> AVLNode<E>?.height: Long get() = this?.height ?: -1

      /**
       * Gets the relative position.
       */
      private var <E> AVLNode<E>?.offset: Long
        get() = this?.relativePosition ?: 0
        set(value) {
          if (this == null) return
          this.relativePosition = value
        }
    }
  }
}