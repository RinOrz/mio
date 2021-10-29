package com.meowool.mio.internal

import com.meowool.mio.Charset
import com.meowool.mio.FileChannel
import com.meowool.mio.IFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * @author 凛 (https://github.com/RinOrz)
 */
internal abstract class CommonFile<Self : IFile<Self>>(chars: CharSequence) :
  IFile<Self>, CommonPath<Self>(chars) {

  override fun text(charset: Charset): String = open { readAll() }

  override fun lines(charset: Charset): Flow<String> = open {
    flow { readLineOrNull()?.also { emit(it) } }
  }

  override fun append(bytes: ByteArray): Self = self {

  }

  override fun append(channel: FileChannel<*>): Self {
    TODO("Not yet implemented")
  }

  override fun append(text: CharSequence, charset: Charset): Self {
    TODO("Not yet implemented")
  }

  override fun append(lines: Iterable<CharSequence>, charset: Charset): Self {
    TODO("Not yet implemented")
  }

  override fun append(lines: Sequence<CharSequence>, charset: Charset): Self {
    TODO("Not yet implemented")
  }

  override fun write(bytes: ByteArray): Self {
    TODO("Not yet implemented")
  }

  override fun write(channel: FileChannel<*>): Self {
    TODO("Not yet implemented")
  }

  override fun write(text: CharSequence, charset: Charset): Self {
    TODO("Not yet implemented")
  }

  override fun write(lines: Iterable<CharSequence>, charset: Charset): Self {
    TODO("Not yet implemented")
  }

  override fun write(lines: Sequence<CharSequence>, charset: Charset): Self {
    TODO("Not yet implemented")
  }
}