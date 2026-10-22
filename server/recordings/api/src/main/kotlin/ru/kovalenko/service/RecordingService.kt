package ru.kovalenko.service

import ru.kovalenko.operations.AddRecording
import ru.kovalenko.operations.DeleteRecording
import ru.kovalenko.operations.GetAllRecordings
import ru.kovalenko.operations.DownloadRecording
import ru.kovalenko.operations.RenameRecording

interface RecordingService {
    suspend fun save(request: AddRecording.Request): AddRecording.Response
    fun delete(request: DeleteRecording.Request): DeleteRecording.Response
    fun getAll(request: GetAllRecordings.Request): GetAllRecordings.Response
    fun rename(request: RenameRecording.Request): RenameRecording.Response
    fun download(request: DownloadRecording.Request): DownloadRecording.Response
}