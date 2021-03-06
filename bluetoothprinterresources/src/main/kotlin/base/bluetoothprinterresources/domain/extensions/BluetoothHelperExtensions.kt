package base.bluetoothprinterresources.domain.extensions

import android.graphics.Bitmap
import arrow.core.Try
import base.bluetoothresources.domain.BluetoothHelper
import base.corextresources.domain.extensions.not
import base.coreresources.extensions.splitIntoSequence
import com.github.icarohs7.unoxcore.toplevel.tryBg
import kotlinx.coroutines.delay

suspend fun BluetoothHelper.printImage(image: Bitmap): Try<Unit> = tryBg {
    image.splitIntoSequence(200).forEach { part ->
        !!write(bitmapToByteArray(part)!!)
        delay(1)
    }
    !!write("\n\n\n\n")
}

private fun bitmapToByteArray(bitmap: Bitmap): ByteArray? {
    return PrinterUtilsJava.decodeBitmap(bitmap)
}