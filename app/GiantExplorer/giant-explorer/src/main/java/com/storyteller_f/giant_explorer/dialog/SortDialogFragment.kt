package com.storyteller_f.giant_explorer.dialog

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import com.storyteller_f.common_ui.CommonDialogFragment
import com.storyteller_f.config_core.ConfigItem
import com.storyteller_f.config_core.DefaultDialog
import com.storyteller_f.file_system.model.FileInfo
import com.storyteller_f.giant_explorer.filter.NameSort
import com.storyteller_f.giant_explorer.filter.SortFactory
import com.storyteller_f.sort_core.config.SortChain
import com.storyteller_f.sort_core.config.SortConfigItem
import com.storyteller_f.sort_ui.SortDialog
import java.io.IOException

val activeSortChains = MutableLiveData<List<SortChain<FileInfo>>>()

class SortDialogFragment : CommonDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?) = setupSortDialog().selfDialog

    @Throws(IOException::class)
    private fun setupSortDialog() = SortDialog(
        requireContext(),
        suffix,
        sortChains,
        listener,
        SortFactory(),
        adapterFactory
    )

    private val listener =
        object : DefaultDialog.Listener<SortChain<FileInfo>, SortConfigItem> {
            override fun onSaveState(oList: List<SortChain<FileInfo>>) =
                oList.map {
                    NameSort.Item()
                }.toMutableList()

            override fun onRestoreState(configItems: List<SortConfigItem>): List<SortChain<FileInfo>> =
                configItems.buildSorts()

            override fun onActiveChanged(activeList: List<SortChain<FileInfo>>) {
                activeSortChains.value = activeList
            }

            override fun onEditingChanged(editing: List<SortChain<FileInfo>>) = Unit
        }

    private val sortChains: List<SortChain<FileInfo>>
        get() = buildList {
            add(NameSort(NameSort.Item()))
        }

    companion object {
        const val suffix = "sort"
        val adapterFactory: RuntimeTypeAdapterFactory<ConfigItem> =
            RuntimeTypeAdapterFactory.of(
                ConfigItem::class.java,
                "sort-item-key"
            )
                .registerSubtype(NameSort.Item::class.java, "name")
    }
}

fun List<SortConfigItem>.buildSorts() =
    map {
        NameSort(NameSort.Item())
    }
