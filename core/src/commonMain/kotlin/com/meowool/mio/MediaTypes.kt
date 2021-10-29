@file:Suppress("SpellCheckingInspection", "MemberVisibilityCanBePrivate")

package com.meowool.mio

/**
 * Represents the media types.
 *
 * @param value mime type values, [for more details](https://en.wikipedia.org/wiki/Media_type).
 *
 * @see IPath.contentType
 *
 * @author 凛 (https://github.com/RinOrz)
 */
sealed class MediaType(val value: List<String>) {
  constructor(vararg values: String) : this(values.toList())

  /**
   * Returns `true` if the given [type] belongs to the media type.
   *
   * For code example:
   * ```
   * val isDirectory = path.contentType in MediaType.Directory
   * // or
   * val isArchive = MediaType.Archive.contains(path.contentType)
   * // or even
   * when(path.contentType) {
   *   in MediaType.Directory -> openDirectory(path)
   *   in MediaType.Audio -> playAudio(path)
   * }
   * ```
   *
   * @see IPath.contentType
   */
  operator fun contains(type: String) : Boolean = value.any {
    if (it.endsWith("*")) type.startsWith(it.removeSuffix("*")) else it == type
  }

  /**
   * For the media types of the directory.
   *
   * [For android details](https://developer.android.com/reference/android/provider/DocumentsContract.Document#MIME_TYPE_DIR)
   */
  object Directory : MediaType(
    "inode/directory",
    "vnd.android.document/directory"
  )

  /**
   * For the media types of the text file.
   */
  object Text : MediaType("text/*")

  /**
   * For the media types of the font file.
   */
  object Font : MediaType(
    "application/font-cff",
    "application/font-off",
    "application/font-sfnt",
    "application/font-ttf",
    "application/font-woff",
    "application/vnd.ms-fontobject",
    "application/vnd.ms-opentype",
    "application/x-font",
    "application/x-font-ttf",
    "application/x-font-woff",
  )

  /**
   * For the media types of the directory.
   *
   * [For android details](https://developer.android.com/reference/android/provider/DocumentsContract.Document#MIME_TYPE_DIR)
   */
  object Audio : MediaType(
    "audio/*",
    "application/ogg",
    "application/x-flac"
  )

  /**
   * For the media types of the image file.
   */
  object Image : MediaType(
    "image/*",
    "application/vnd.oasis.opendocument.graphics",
    "application/vnd.oasis.opendocument.graphics-template",
    "application/vnd.oasis.opendocument.image",
    "application/vnd.oasis.opendocument.image-template",
    "application/vnd.stardivision.draw",
    "application/vnd.sun.xml.draw",
    "application/vnd.sun.xml.draw.template",
    "application/vnd.visio",
  )

  /**
   * For the media types of the video file.
   */
  object Video : MediaType(
    "video/*",
    "application/x-quicktimeplayer",
    "application/x-shockwave-flash",
  )

  /**
   * For the media types of the android application package format.
   *
   * @see Archive
   */
  object Apk : MediaType("application/vnd.android.package-archive")

  /**
   * For the media types of the archive format.
   *
   * [For more details](https://en.wikipedia.org/wiki/List_of_archive_formats)
   *
   * @see Apk
   */
  object Archive : MediaType(
    "application/vnd.android.package-archive",
    "application/x-archive",
    "application/x-cpio",
    "application/x-shar",
    "application/x-iso9660-image",
    "application/x-sbx",
    "application/x-tar",
    "application/gzip",
    "application/x-lzip",
    "application/x-lzma",
    "application/x-lzop",
    "application/x-snappy-framed",
    "application/x-xz",
    "application/x-compress",
    "application/zstd",
    "application/x-7z-compressed",
    "application/x-ace-compressed",
    "application/x-astrotite-afa",
    "application/x-alz-compressed",
    "application/x-freearc",
    "application/x-arj",
    "application/x-b1",
    "application/java-archive",
    "application/x-rar-compressed",
    "application/x-lzh",
    "application/x-lzx",
    "application/x-xar",
    "application/zip",
    "application/mac-binhex40",
    "application/rar",
    "application/vnd.debian.binary-package",
    "application/vnd.ms-cab-compressed",
    "application/vnd.rar",
    "application/x-apple-diskimage",
    "application/x-bzip",
    "application/x-bzip2",
    "application/x-deb",
    "application/x-debian-package",
    "application/x-gtar",
    "application/x-gtar-compressed",
    "application/x-gzip",
    "application/x-java-archive",
    "application/x-lha",
    "application/x-stuffit",
    "application/x-webarchive",
    "application/x-webarchive-xml",
  )
}

/**
 * Returns `true` if the content type of this path is an archive file.
 */
val Path.isArchiveFile get() = contentType in MediaType.Archive

/**
 * Returns `true` if the content type of this path is an apk file.
 */
val Path.isApkFile get() = contentType in MediaType.Apk