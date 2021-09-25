import ui.MainWindow
import util.ByteManipulation
import util.Log
import java.nio.ByteOrder

suspend fun main() {
    //val mainWindow = MainWindow()
    //mainWindow.start()
    val a = ByteArray(8)
    ByteManipulation.writeLongToArray(123456789, a, 0, ByteOrder.BIG_ENDIAN)
    //ByteManipulation.writeLongToArray(2, a, 0, ByteOrder.LITTLE_ENDIAN)
    val o = ByteManipulation.readLongFromArray(a, 0, ByteOrder.BIG_ENDIAN)
    Log.Companion.info("Test")
}