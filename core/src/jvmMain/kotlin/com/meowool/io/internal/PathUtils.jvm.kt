@file:Suppress("NOTHING_TO_INLINE", "RemoveRedundantQualifierName")

package com.meowool.io.internal

import com.meowool.io.DirectoryNotEmptyException
import com.meowool.io.File
import com.meowool.io.IoFile
import com.meowool.io.NioPath
import com.meowool.io.NoSuchPathException
import com.meowool.io.ParentDirectoryNotExistsException
import com.meowool.io.Path
import com.meowool.io.PathAlreadyExistsException
import com.meowool.io.PathExistsAndIsNotFileException
import com.meowool.io.asPath
import com.meowool.io.toIoFile
import com.meowool.io.toMioPath
import com.meowool.io.toNioPath
import com.meowool.sweekt.String
import com.meowool.sweekt.iteration.toArray
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

internal actual val userHome: String = System.getProperty("user.home")
internal actual val currentDir: String = System.getProperty("user.dir")

internal fun NioPath(first: CharSequence, vararg more: CharSequence): NioPath = Paths.get(
  first.toString(),
  *more.map(::String).toArray()
)