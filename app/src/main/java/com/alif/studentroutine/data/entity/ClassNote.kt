package com.alif.studentroutine.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "class_notes",
    foreignKeys = [
        ForeignKey(
            entity = ClassItem::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["classId"])]
)
data class ClassNote(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val classId: Int,
    val title: String = "",
    val textContent: String = "",
    val checklistJson: String = "[]",   // JSON: [{text, isDone}]
    val imagePaths: String = "[]",      // JSON: ["/internal/path/img.jpg"]
    val filePaths: String = "[]",       // JSON: ["/internal/path/file.pdf"]
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)