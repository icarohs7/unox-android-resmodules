package base.dataresources.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TestClass(@PrimaryKey val id: Int = 0, val message: String)