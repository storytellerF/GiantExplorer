package com.storyteller_f.giant_explorer.model

import com.storyteller_f.file_system.model.FileInfo
import com.storyteller_f.ui_list.core.Model

data class FileModel(
    val item: FileInfo,
    val name: String,
    val fullPath: String,
    /**
     * -1 代表当前大小未知
     */
    val size: Long,
    val isHidden: Boolean,
    val isSymLink: Boolean,
    val torrentName: String?,
    val md: String?,
    val formattedSize: String,
) : Model {
    override fun commonId() = fullPath
}
