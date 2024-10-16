import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.*
import info.media.ImageInfo
import okhttp3.internal.format
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jcodec.api.FrameGrab
import org.jcodec.common.DemuxerTrack
import org.jcodec.common.VideoCodecMeta
import org.jcodec.common.io.NIOUtils
import org.jcodec.scale.AWTUtil
import org.jetbrains.skia.*
import org.jetbrains.skiko.toImage
import properties.*
import properties.Properties
import properties.data.MediaData
import properties.data.TagData
import properties.settings.Settings
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Image.SCALE_SMOOTH
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
import javax.imageio.ImageReader


/* -= shortcuts =- */
val lang: Language get() = Properties.language()
val settings: Settings get() = Properties.settings()
val style: Style get() = Properties.style()
val mediaData: MediaData get() = Properties.mediaData()
val tagData: TagData get() = Properties.tagData()


/* -= image functions =- */
val emptyImageBitmap: ImageBitmap = ImageBitmap(1, 1)

fun getImageBitmap(imageFile: File): ImageBitmap? {
    return if (imageFile.exists())
        try {
            Image.makeFromEncoded(imageFile.readBytes()).toComposeImageBitmap()
        } catch (e: OutOfMemoryError) {
            null
        } catch (e: Exception) {
            null
        }
    else
        null
}

fun getImageBitmap(imagePath: String): ImageBitmap? = getImageBitmap(File(imagePath))

fun getImageBitmap(imageBytes: ByteArray): ImageBitmap = Image.makeFromEncoded(imageBytes).toComposeImageBitmap()

fun ImageBitmap.savePngTo(file: File) {
    val data = this.toAwtImage().toImage().encodeToData(EncodedImageFormat.PNG, 100)
    data?.let { file.writeBytes(it.bytes) }
}

fun ImageBitmap.savePngTo(path: String) = saveWebPTo(File(path))

fun ImageBitmap.saveWebPTo(file: File) {
    val data = this.toAwtImage().toImage().encodeToData(EncodedImageFormat.WEBP, 93)
    data?.let { file.writeBytes(it.bytes) }
}

fun ImageBitmap.saveWebPTo(path: String) = saveWebPTo(File(path))

fun ImageBitmap.encodeToWebP(): ByteArray? {
    return this.toAwtImage().toImage().encodeToData(EncodedImageFormat.WEBP, 95)?.bytes
}

fun ImageBitmap.scale(width: Int, height: Int): ImageBitmap {
    val image = this.toAwtImage().getScaledInstance(width, height, SCALE_SMOOTH)
    val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g2d = bufferedImage.createGraphics()
    g2d.drawImage(image, 0, 0, null)
    g2d.dispose()
    return bufferedImage.toComposeImageBitmap()
}

fun ImageBitmap.scaleToMaxValues(maxWidth: Int, maxHeight: Int): ImageBitmap {
    val scaledDownSize = calculateScaledDownSize(width, height, maxWidth, maxHeight)
    val newWidth = scaledDownSize.first
    val newHeight = scaledDownSize.second
    return this.scale(newWidth, newHeight)
}

private fun calculateScaledDownSize(
    originalWidth: Int,
    originalHeight: Int,
    maxWidth: Int,
    maxHeight: Int
): Pair<Int, Int> {
    val widthRatio = originalWidth.toDouble() / maxWidth.toDouble()
    val heightRatio = originalHeight.toDouble() / maxHeight.toDouble()
    val scaleRatio = maxOf(widthRatio, heightRatio)

    val scaledWidth = (originalWidth.toDouble() / scaleRatio).toInt()
    val scaledHeight = (originalHeight.toDouble() / scaleRatio).toInt()

    return Pair(scaledWidth, scaledHeight)
}

fun File.loadAllImages(): Map<String, ImageBitmap> {
    val files = this.listFiles() ?: return mapOf()

    val list = mutableMapOf<String, ImageBitmap>()
    for (f in files) {
        try {
            list[f.name] = getImageBitmap(f) ?: continue
        } catch (e: Exception) {
        }
    }

    return list
}

fun copyAndGetImage(file: File, to: File): Pair<String, ImageBitmap>? {
    getImageBitmap(file)?.let { img ->
        val name = uniqueName(file.nameWithoutExtension, file.extension, to)
        file.copyTo(File("${to.path}/$name.${file.extension}"))
        return Pair("$name.${file.extension}", img)

    } ?: return null
}

@Deprecated(
    "ImageInfo is not the most reliable way to calculate weight anymore. Use ImageBitmap.calculateWeight() instead.",
    ReplaceWith("thumbnail?.calculateWeight()"),
)
fun ImageInfo.calculateWeight(): Float = (width.toDouble() / height.toDouble()).toFloat()

fun ImageBitmap.calculateWeight(): Float = (width.toDouble() / height.toDouble()).toFloat()

// This code is from the comment https://stackoverflow.com/a/1560052 and auto converted to Kotlin ¯\_(ツ)_/¯
fun getImageDimensions(file: File): Dimension? {
    ImageIO.createImageInputStream(file).use { `in` ->
        val readers: Iterator<ImageReader> = ImageIO.getImageReaders(`in`)
        if (readers.hasNext()) {
            val reader: ImageReader = readers.next()
            try {
                reader.input = `in`
                return Dimension(reader.getWidth(0), reader.getHeight(0))
            } finally {
                reader.dispose()
            }
        }
    }

    return null
}

fun getImageDimensions(path: String): Dimension? = getImageDimensions(File(path))

fun getVideoDimensions(file: File): Dimension? {
    try {
        val grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file))
        val videoCodecMeta: VideoCodecMeta = grab.videoTrack.meta.videoCodecMeta ?: return null
        return Dimension(videoCodecMeta.size.width, videoCodecMeta.size.height)

    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun getVideoDimensions(path: String): Dimension? = getVideoDimensions(File(path))

fun isVideoFileSupported(file: File): Boolean {
    try {
        val grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file))
        return grab.videoTrack.meta.codec != null
    } catch (e: Exception) {
        return false
    }
}

fun isVideoFileSupported(path: String): Boolean = isVideoFileSupported(File(path))

fun getFirstFrame(file: File): ImageBitmap? = getVideoFrame(file, 0)

fun getFirstFrame(path: String): ImageBitmap? = getFirstFrame(File(path))

fun getVideoFrame(file: File, frame: Int): ImageBitmap? {
    try {
        val picture = FrameGrab.getFrameFromFile(file, frame)
        val bufferedImage: BufferedImage = AWTUtil.toBufferedImage(picture)
        return bufferedImage.toComposeImageBitmap()

    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun getVideoFrame(path: String, frame: Int): ImageBitmap? = getVideoFrame(File(path), frame)

fun getVideoFrameCount(file: File): Int {
    try {
        val grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file))
        return grab.videoTrack.meta.totalFrames
    } catch (e: Exception) {
        e.printStackTrace()
        return 0
    }
}

fun getVideoFrameCount(path: String): Int = getVideoFrameCount(File(path))

fun getVideoDuration(file: File): Long? {
    try {
        val grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file))
        val videoTrack: DemuxerTrack = grab.videoTrack
        return (videoTrack.meta.totalDuration * 1000).toLong()

    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun getVideoDuration(path: String): Long? = getVideoDuration(File(path))

fun openVideoFile(file: File) {
    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()
        if (file.exists()) {
            desktop.open(file) // Opens the file with the default media player
        } else {
            println("The file does not exist.")
        }
    } else {
        println("Desktop operations are not supported on this system.")
    }
}

fun openVideoFile(path: String) = openVideoFile(File(path))

fun isAudioFileSupported(file: File): Boolean {
    try {
        AudioFileIO.read(file)
    } catch (e: Exception) {
        return false
    }
    return true
}

fun isAudioFileSupported(path: String): Boolean = isAudioFileSupported(File(path))

fun getAudioDuration(file: File): Long? {
    try {
        val audioFile = AudioFileIO.read(file)
        return audioFile.audioHeader.trackLength * 1000L

    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun getAudioDuration(path: String): Long? = getAudioDuration(File(path))

fun getAudioCover(file: File): ImageBitmap? {
    val audioFile = AudioFileIO.read(file)
    if (audioFile.tag.artworkList.isEmpty()) return null
    return audioFile.tag.firstArtwork?.image?.toComposeImageBitmap()
}

fun getAudioCover(path: String): ImageBitmap? = getAudioCover(File(path))

fun getAudioArtist(file: File): String? {
    val audioFile = AudioFileIO.read(file)
    return audioFile.tag.getFirst(FieldKey.ARTIST)
}

fun getAudioArtist(path: String): String? = getAudioArtist(File(path))

fun getAudioTitle(file: File): String? {
    val audioFile = AudioFileIO.read(file)
    return audioFile.tag.getFirst(FieldKey.TITLE)
}

fun getAudioTitle(path: String): String? = getAudioTitle(File(path))

fun openAudioFile(file: File) {
    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()
        if (file.exists()) {
            desktop.open(file) // Opens the file with the default media player
        } else {
            println("The file does not exist.")
        }
    } else {
        println("Desktop operations are not supported on this system.")
    }
}

fun openAudioFile(path: String) = openAudioFile(File(path))


/* -= additional functions =- */
fun Int.roundToStep(step: Int): Int {
    val steps = this / step
    val lastStep = this % step
    val centerOfStep = step.toFloat() / 2F

    return (steps + if (lastStep > centerOfStep) 1 else 0) * step
}

fun Double.roundPlaces(places: Int): Double = "%.${places}f".format(this).toDouble()

fun Long.toTimeString(): String {
    val date = Date(this)
    val format = SimpleDateFormat("hh:mma dd/MM/yyyy")
    return format.format(date)
}

fun decodeAllUTF(string: String): String {
    var newString = string
    val matches = Regex("\\\\u[0-9a-z]{4}").findAll(string)

    matches.forEach {
        val char = Integer.parseInt(it.value.drop(2), 16)
        newString = newString.replace(it.value, Char(char).toString())
    }

    return newString
}

fun String.runCommand(workingDir: File): String? {
    return try {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        //proc.waitFor(60, TimeUnit.MINUTES)
        proc.inputStream.bufferedReader().readText()
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

/**
 * @param baseName base name of the file (Example: "name")
 * @param extension extension of the file (Example: "webp")
 * @param folder what folder create unique name for
 *
 * @return unique name without extension
 */
fun uniqueName(baseName: String, extension: String, folder: File): String {
    var i = 0
    while (File("${folder.absolutePath}/${baseName}${if (i > 0) "_" else ""}${if (i > 0) i else ""}.$extension").exists()) { i++ }
    return "${baseName}${if (i > 0) "_" else ""}${if (i > 0) i else ""}"
}

fun uniqueName(name: String, list: List<String>): String {
    var i = 0
    while ("${name}${if (i > 0) "_" else ""}${if (i > 0) i else ""}" in list) { i++ }
    return "${name}${if (i > 0) "_" else ""}${if (i > 0) i else ""}"
}

/**
 * @param baseName base name of the file (Example: "name")
 * @param extension extension of the file (Example: "webp")
 * @param folder what folder create unique name for
 *
 * @return unique name without extension
 */
fun uniqueNameIncludingZero(baseName: String, extension: String, folder: File): String {
    var i = 0
    while (File("${folder.absolutePath}/${baseName}${if (i > 0) "_" else ""}$i.$extension").exists()) { i++ }
    return "${baseName}${if (i > 0) "_" else ""}$i"
}

fun uniqueNameIncludingZero(name: String, list: List<String>): String {
    var i = 0
    while ("${name}${if (i > 0) "_" else ""}$i" in list) { i++ }
    return "${name}${if (i > 0) "_" else ""}$i"
}

fun String.toFileName(): String = this
    .replace(" ", "_")
    .replace("<", "")
    .replace(">", "")
    .replace(":", "")
    .replace("\"", "")
    .replace("/", "")
    .replace("\\", "")
    .replace("|", "")
    .replace("?", "")
    .replace("*", "")

fun String.capitalizeEachWord(): String = this
    .split(" ")
    .joinToString(" ") { it.replaceFirstChar { char -> char.titlecase() } }

fun Long.formatTime(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return if (hours == 0L) {
        "${format("%02d", minutes % 60)}:${format("%02d", seconds % 60 % 60)}"
    }else {
        "$hours:${format("%02d", minutes % 60)}:${format("%02d", seconds % 60 % 60)}"
    }
}

fun openWebpage(uri: URI?): Boolean {
    val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
            desktop.browse(uri)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return false
}

fun openWebpage(url: URL): Boolean {
    try {
        return openWebpage(url.toURI())
    } catch (e: URISyntaxException) {
        e.printStackTrace()
    }
    return false
}

fun <K, V> Map<K, V>.toState(): SnapshotStateMap<K, V> {
    val newList = mutableStateMapOf<K, V>()
    forEach {
        newList[it.key] = it.value
    }
    return newList
}

fun <T> List<T>.toState(): SnapshotStateList<T> = mutableStateListOf<T>().also { newList ->
    this.forEach {
        newList.add(it)
    }
}

fun Map<String, *>.toStringList(): List<String> {
    val list = mutableListOf<String>()
    forEach { (string, _) -> list.add(string) }
    return list
}

fun <T> MutableList<T>.swap(idx1: Int, idx2: Int): MutableList<T> = apply {
    val t = this[idx1]
    this[idx1] = this[idx2]
    this[idx2] = t
}

fun <T> Collection<T>.containsAtLeastOne(elements: Collection<T>): Boolean {
    for (element in elements) {
        if (this.contains(element)) return true
    }
    return false
}

fun File.getDirectorySize(): Long {
    var size: Long = 0
    if (exists()) {
        val files = listFiles()
        if (files != null) {
            for (file in files) {
                size += if (file.isDirectory) {
                    file.getDirectorySize()
                } else {
                    file.length()
                }
            }
        }
    }
    return size
}