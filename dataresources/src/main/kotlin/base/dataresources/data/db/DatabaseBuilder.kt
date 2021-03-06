package base.dataresources.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/** Helper method used to create a RoomDatabase instance */
inline fun <reified T : RoomDatabase> Context.buildDatabase(
    destructiveMigration: Boolean = false,
    dbFileName: String = "app.db"
): RoomDatabase.Builder<T> {
    val builder = Room.databaseBuilder(this, T::class.java, dbFileName)
    return if (!destructiveMigration) builder
    else builder.fallbackToDestructiveMigration()
}