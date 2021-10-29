@file:OptIn(ExperimentalStdlibApi::class)
@file:Suppress("ConvertTwoComparisonsToRangeCheck", "MemberVisibilityCanBePrivate",
  "NOTHING_TO_INLINE")

package com.meowool.mio.internal

import com.meowool.mio.channel.ByteOrder
import com.meowool.sweekt.array.ByteArrayBuilder.Companion.MaxArraySize
import com.meowool.sweekt.array.buildByteArray
import com.meowool.sweekt.throwIf

internal class PieceBuffer(private val raw: DataBuffer<*>) : MutableDataBuffer<PieceBuffer> {

  /**
   * Append data only.
   */
  private val new = ByteArrayBuffer(8192).apply { order = raw.order }

  /**
   * Information of all the pieces.
   * The entire raw data is filled by default.
   */
  val pieces = TreeList<Piece>().apply {
    this += Piece(
      relativeStart = 0,
      relativeEnd = raw.size,
      type = Piece.Type.Raw
    ).apply { absoluteStart = 0 }
  }

  private var _size: Long = LongPlaceholder
    get() = field.ifPlaceholder {
      raw.size.also { _size = it }
    }

  override var size: Long
    set(newSize) {
      require(newSize >= 0) { "size:$newSize < 0" }
      when {
        newSize == 0L -> clear()
        // Grow size
        newSize > _size -> pieces += Piece(
          relativeStart = 0,
          relativeEnd = newSize - _size,
          type = Piece.Type.Empty
        )
        // Truncate size
        else -> buildList<Piece>(pieces.size.toInt()) {
          val retained = this
          focus(newSize - 1) { pieceIndex, piece ->
            requireNotNull(piece)
            piece.relativeEnd = piece.relativeBy(newSize)
            // If the focusing piece is in the middle, discard the pieces on the right
            if (pieceIndex != pieces.lastIndex) {
              (0 until pieceIndex).forEach { retained.add(pieces[it]) }
              retained.add(piece)
              pieces.clear()
              pieces.addAll(retained)
            }
          }
        }
      }
      updateSize { newSize }
    }
    get() = _size

  override var order: ByteOrder
    get() = raw.order
    set(value) {
      raw.order = value
      new.order = value
    }

  override fun getByte(index: Long): Byte =
    focus(index).run { selectBuffer()?.getByte(relativeBy(index)) ?: EmptyByte }

  override fun getChar(index: Long): Char =
    focus(index).run { selectBuffer()?.getChar(relativeBy(index)) ?: EmptyChar }

  override fun getInt(index: Long): Int =
    focus(index).run { selectBuffer()?.getInt(relativeBy(index)) ?: EmptyInt }

  override fun getLong(index: Long): Long =
    focus(index).run { selectBuffer()?.getLong(relativeBy(index)) ?: EmptyLong }

  override fun getFloat(index: Long): Float =
    focus(index).run { selectBuffer()?.getFloat(relativeBy(index)) ?: EmptyFloat }

  override fun getShort(index: Long): Short =
    focus(index).run { selectBuffer()?.getShort(relativeBy(index)) ?: EmptyShort }

  override fun getDouble(index: Long): Double =
    focus(index).run { selectBuffer()?.getDouble(relativeBy(index)) ?: EmptyDouble }

  override fun getBytes(index: Long, count: Int): ByteArray = buildByteArray(count) {
    focus(index).also {
      val startIndex = it.relativeBy(index)
      val endIndex = index + count
      when {
        it.relativeEnd > endIndex -> {
          val part1Size = (it.size - startIndex).toInt()
          val part2Size = count - part1Size
          append(it.selectBuffer()?.getBytes(index = startIndex, count = part1Size)
            ?: ByteArray(part1Size))
          append(focus(endIndex).selectBuffer()?.getBytes(0, count = part2Size)
            ?: ByteArray(part2Size))
        }
        else -> append(it.selectBuffer()?.getBytes(startIndex, count)
          ?: ByteArray(count))
      }
    }
  }

  override fun getAllBytes(): ByteArray {
    throwIf(size > MaxArraySize) { OutOfMemoryError("Required array size too large") }
    return buildByteArray(size.toInt()) {
      pieces.forEach {
        append(it.selectBuffer()?.getBytes(it.relativeStart, it.size.toInt())
          ?: ByteArray(it.size.toInt()))
      }
    }
  }

  override fun put(index: Long, data: Byte): PieceBuffer = apply {
    put(index, { new.add(data) }, { put(it, data) })
    if (index >= size) updateSize { index + Byte.SIZE_BYTES }
  }

  override fun put(index: Long, data: Short): PieceBuffer = apply {
    put(index, { new.add(data) }, { put(it, data) })
    if (index >= size) updateSize { index + Short.SIZE_BYTES }
  }

  override fun put(index: Long, data: Char): PieceBuffer = apply {
    put(index, { new.add(data) }, { put(it, data) })
    if (index >= size) updateSize { index + Char.SIZE_BYTES }
  }

  override fun put(index: Long, data: Int): PieceBuffer = apply {
    put(index, { new.add(data) }, { put(it, data) })
    if (index >= size) updateSize { index + Int.SIZE_BYTES }
  }

  override fun put(index: Long, data: Float): PieceBuffer = apply {
    put(index, { new.add(data) }, { put(it, data) })
    if (index >= size) updateSize { index + Float.SIZE_BYTES }
  }

  override fun put(index: Long, data: Long): PieceBuffer = apply {
    put(index, { new.add(data) }, { put(it, data) })
    if (index >= size) updateSize { index + Long.SIZE_BYTES }
  }

  override fun put(index: Long, data: Double): PieceBuffer = apply {
    put(index, { new.add(data) }, { put(it, data) })
    if (index >= size) updateSize { index + Double.SIZE_BYTES }
  }

  override fun insert(index: Long, data: Byte): PieceBuffer = apply {
    new.add(data)
    insert(index)
    when {
      index >= size -> updateSize { index + Byte.SIZE_BYTES }
      else -> increaseSize { Byte.SIZE_BYTES }
    }
  }

  override fun insert(index: Long, data: Short): PieceBuffer = apply {
    new.add(data)
    insert(index)
    when {
      index >= size -> updateSize { index + Short.SIZE_BYTES }
      else -> increaseSize { Short.SIZE_BYTES }
    }
  }

  override fun insert(index: Long, data: Char): PieceBuffer = apply {
    new.add(data)
    insert(index)
    when {
      index >= size -> updateSize { index + Char.SIZE_BYTES }
      else -> increaseSize { Char.SIZE_BYTES }
    }
  }

  override fun insert(index: Long, data: Int): PieceBuffer = apply {
    new.add(data)
    insert(index)
    when {
      index >= size -> updateSize { index + Int.SIZE_BYTES }
      else -> increaseSize { Int.SIZE_BYTES }
    }
  }

  override fun insert(index: Long, data: Float): PieceBuffer = apply {
    new.add(data)
    insert(index)
    when {
      index >= size -> updateSize { index + Float.SIZE_BYTES }
      else -> increaseSize { Float.SIZE_BYTES }
    }
  }

  override fun insert(index: Long, data: Long): PieceBuffer = apply {
    new.add(data)
    insert(index)
    when {
      index >= size -> updateSize { index + Long.SIZE_BYTES }
      else -> increaseSize { Long.SIZE_BYTES }
    }
  }

  override fun insert(index: Long, data: Double): PieceBuffer = apply {
    new.add(data)
    insert(index)
    when {
      index >= size -> updateSize { index + Double.SIZE_BYTES }
      else -> increaseSize { Double.SIZE_BYTES }
    }
  }

  override fun remove(index: Long, count: Long): PieceBuffer = apply {
    checkNegativeIndex(index)
    throwIf(index + count > size) { IndexOutOfBoundsException("(index:$index + count:$count) > buffer size:$size") }
    focus(index) { pieceIndex, piece ->
      piece ?: throw NoSuchElementException("index: $index")
      when (index) {
        piece.absoluteStart -> piece.relativeStart += count
        piece.absoluteEndIndex -> piece.relativeEnd -= count
        else -> {
          val part2 = piece.split(index)
          /**
           * For example, remove by index `3`, count `2`:
           *   pieces: [0, 1, 2, 3] [3, 4, 5, 6, 7]
           *   remove:               ^^^^
           *   part2:                      =======
           */
          part2.relativeStart += count
          pieces.addAt(pieceIndex + 1, part2)
        }
      }
    }
    decreaseSize { count }
  }

  override fun clear(): PieceBuffer = apply {
    pieces.clear()
    new.clear()
    updateSize { 0 }
  }

  /**
   * Puts a piece at the specified absolute [absoluteIndex].
   */
  private inline fun put(
    absoluteIndex: Long,
    addToBuffer: () -> Unit,
    putToBuffer: DataBuffer<*>.(relativeIndex: Long) -> Unit,
  ) {
    checkNegativeIndex(absoluteIndex)
    if (absoluteIndex >= size) {
      addToBuffer()
      insert(absoluteIndex)
      return
    }

    focus(absoluteIndex) { pieceIndex, piece ->
      when (val buffer = piece!!.selectBuffer()) {

        /**
         * Empty piece need to adopt a scheme with split and crop the empty piece and then
         * insert the piece.
         *
         * For example, put to index `2`:
         * Original: Empty(0..5)
         * Result:   Empty(0..1) Piece(2..3) Empty(3..5)
         */
        null -> {
          addToBuffer()
          val newSnapshotSize = newSnapshotSize()
          // If the empty piece size is the same as the piece to be put, it can be replaced directly
          if (newSnapshotSize == piece.size) {
            pieces[pieceIndex] = newSnapshotPiece()
          } else when (absoluteIndex) {
            piece.absoluteStart -> {
              pieces.addAt(pieceIndex, newSnapshotPiece())
              piece.relativeStart += newSnapshotSize
            }
            piece.absoluteEndIndex -> {
              pieces.addAt(pieceIndex + 1, newSnapshotPiece())
              piece.relativeEnd -= newSnapshotSize
            }
            else -> {
              val part2 = piece.split(absoluteIndex).apply { relativeStart++ }
              // Insert the new piece
              pieces.addAt(pieceIndex + 1, newSnapshotPiece())
              // Insert the part 2 of split
              pieces.addAt(pieceIndex + 2, part2)
              piece.relativeEnd -= newSnapshotSize - 1
            }
          }
        }

        else -> buffer.putToBuffer(piece.relativeBy(absoluteIndex))
      }
    }
  }

  /** Returns a piece, created by the snapshot of the last data added to the [new] buffer. */
  private inline fun newSnapshotPiece() = Piece(relativeStart = new.oldSize, relativeEnd = new.size)

  /** Returns the size by the snapshot of the last data added to the [new] buffer. */
  private inline fun newSnapshotSize() = new.size - new.oldSize

  /**
   * Inserts a piece at the specified [absoluteIndex].
   */
  private fun insert(absoluteIndex: Long) {
    checkNegativeIndex(absoluteIndex)

    when {
      /** Insert at the beginning. */
      absoluteIndex == 0L -> pieces.addAt(index = 0, element = newSnapshotPiece())

      /** Append to the ending. */
      absoluteIndex == size -> {
        val last = pieces.last()

        /**
         * If the last piece is the data in the end of the `new` buffer, and the data to be added
         * is coherent in the buffer, directly modify the end index to improve performance.
         *
         * For example, the following situation:
         *   end of last piece data: # (8..9)
         *   new buffer:             old_data#Abc
         *   last size to new size:           ^^^ (9..12)
         */
        if (last.type == Piece.Type.New && last.relativeEnd == new.oldSize) {
          last.relativeEnd = new.size
        } else {
          pieces += newSnapshotPiece()
        }
      }

      /**
       * If the index exceeds more than one position of size, it needs to be fill the gap
       * with empty piece.
       *
       * For example, add new piece to index `10`:
       *   pieces:           [0, 4] [4, 8] [8, 9] [10]
       *   fill empty piece:               ^^^^^^
       *   insert piece:                          ^^^^
       */
      absoluteIndex > size -> {
        pieces += Piece(
          relativeStart = 0,
          relativeEnd = absoluteIndex - size,
          type = Piece.Type.Empty
        )
        pieces += newSnapshotPiece()
      }

      /**
       * Insert in the middle.
       *
       * Example:
       * Original:
       *   raw buffer: DATA
       *   new buffer: ignoreQAQ
       *   pieces:  ...(relative 0..4, absolute 0..4, Type.Raw) -> Data
       *            ...(relative 6..9, absolute 4..7)           -> QAQ
       *
       * Append data `_R_` to `new` buffer:
       *   new buffer: ignoreQAQ_R_
       *   append piece:        ^^^ (relative 9..12)
       *
       * Splitting the range of the piece and then insert `append-piece` to index `5`:
       *   pieces:  ...(relative 0..4, absolute 0..4, Type.Raw) -> Data
       *            ...(relative 6..7, absolute 4..5)           -> Q
       *            ...(relative 9..12, absolute 5..8)          -> _R_
       *            ...(relative 7..9, absolute 8..10)          -> AQ
       *
       * Result:       DataQ_R_AQ
       */
      else -> focus(absoluteIndex) { pieceIndex, piece ->
        requireNotNull(piece)
        when (absoluteIndex) {
          // If at the piece start, it can be directly inserted into the list without splitting
          piece.absoluteStart -> pieces.addAt(pieceIndex, newSnapshotPiece())
          // If at the piece end, it can be directly inserted into the list without splitting
          piece.absoluteEndIndex -> pieces.addAt(pieceIndex + 1, newSnapshotPiece())
          else -> {
            val part2 = piece.split(absoluteIndex)
            // Insert the part 2 of split
            pieces.addAt(pieceIndex + 1, part2)
            // Insert the new piece
            pieces.addAt(pieceIndex + 1, newSnapshotPiece())
          }
        }
      }
    }
  }

  private inline fun updateSize(newSize: () -> Long) {
    _size = newSize()
  }

  private inline fun increaseSize(increment: () -> Int) {
    _size += increment()
  }

  private inline fun decreaseSize(increment: () -> Long) {
    _size -= increment()
  }

  private fun checkNegativeIndex(absoluteIndex: Long) {
    throwIf(absoluteIndex < 0) { IndexOutOfBoundsException("index:$absoluteIndex") }
  }

  /** Focus to the piece of the specified [absoluteIndex]. */
  private inline fun <R> focus(
    absoluteIndex: Long,
    block: (pieceIndex: Long, piece: Piece?) -> R,
  ): R = when (absoluteIndex) {
    0L, in pieces.first() -> block(0, pieces.first())
    else -> {
      var previous = 0L
      pieces.forEachIndexed { focusing, piece ->
        // Recalculate the absolute indices
        piece.absoluteStart = previous
        previous += piece.size
        if (absoluteIndex in piece) {
          return block(focusing, piece)
        }
      }
      block(-1, null)
    }
  }

  private fun focusOrNull(absoluteIndex: Long): Piece? = focus(absoluteIndex) { _, focused -> focused }

  private fun focus(absoluteIndex: Long): Piece = focusOrNull(absoluteIndex)
    ?: throw IllegalArgumentException("No piece matching the specified index ($absoluteIndex) was found")

  /** Returns the buffer to which this piece belongs */
  fun Piece.selectBuffer(): DataBuffer<*>? = when (type) {
    Piece.Type.Raw -> raw
    Piece.Type.New -> new
    Piece.Type.Empty -> null
  }

  inline fun forEach(block: (Byte) -> Unit) = pieces.forEach {
    val buffer = it.selectBuffer()
    for (index in it.relativeStart until it.relativeEnd) {
      block(buffer?.getByte(index) ?: EmptyByte)
    }
  }

  /**
   * The piece information pointing to the buffer data.
   *
   * @param relativeStart the starting index relative to the pointing data.
   * @param relativeEnd the ending index (exclusive) relative to the pointing data.
   * @param type pointing to [raw] data or new [new] or empty.
   * @param absoluteStart The absolute starting index in [pieces].
   */
  data class Piece(
    var relativeStart: Long,
    var relativeEnd: Long,
    val type: Type = Type.New,
    var absoluteStart: Long = LongPlaceholder,
  ) {

    /** The absolute ending index (exclusive) in [pieces]. */
    val absoluteEnd: Long get() = absoluteStart + size

    val absoluteEndIndex: Long get() = absoluteEnd - 1

    /** The actual data size of this piece. */
    val size: Long get() = relativeEnd - relativeStart

    /**
     * Splits this piece as two parts:
     * 1. Changes this piece as the first part of the split.
     * 2. Returns the second part of the split.
     *
     * Example:
     * ```
     * Original:
     *   pieces:  ...(relative 6..11, absolute 0..5)
     *
     * Split at absolute index `2`:
     *   pieces:  ...(relative 6..8, absolute 0..2)  // change
     *            ...(relative 8..11, absolute 2..5) // return
     *
     * But, split at absolute index `6` or `11`:
     *   pieces:  ...(relative 6..11, absolute 0..5) // no change
     *            ...(relative 6..11, absolute 0..5) // invalid
     * ```
     *
     * @return part 2 of the split
     */
    fun split(absoluteIndex: Long): Piece {
      val relativeIndex = relativeBy(absoluteIndex)
      // Create the part 2 of split to return
      val part2 = this.copy(relativeStart = relativeIndex)
      // Change the part 1 of split
      this.relativeEnd = relativeIndex
      return part2
    }

    /** Returns the index value relative to this piece according to the specified [absoluteIndex] */
    fun relativeBy(absoluteIndex: Long): Long = relativeStart + (absoluteIndex - absoluteStart)

    /** Returns true if the specified [index] is within the range of this piece. */
    operator fun contains(index: Long): Boolean = index >= absoluteStart && index < absoluteEnd

    override fun toString(): String = "$type: " +
      "relative = $relativeStart..$relativeEnd, " +
      "absolute = $absoluteStart..$absoluteEnd"

    enum class Type { Raw, New, Empty }
  }

  companion object {
    const val EmptyByte: Byte = 0
    const val EmptyChar: Char = 0.toChar()
    const val EmptyInt: Int = 0
    const val EmptyLong: Long = 0
    const val EmptyFloat: Float = 0F
    const val EmptyShort: Short = 0
    const val EmptyDouble: Double = .0
  }
}