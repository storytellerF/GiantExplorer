package com.storyteller_f.giant_explorer.control.plugin

import android.os.Bundle
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.storyteller_f.common_pr.response
import com.storyteller_f.common_ui.CommonActivity
import com.storyteller_f.common_ui.request
import com.storyteller_f.file_system.ensureFile
import com.storyteller_f.file_system.getFileInstance
import com.storyteller_f.file_system.instance.FileInstance
import com.storyteller_f.file_system.operate.ScopeFileCopyOp
import com.storyteller_f.giant_explorer.R
import com.storyteller_f.giant_explorer.databinding.ActivityPluginManageBinding
import com.storyteller_f.giant_explorer.dialog.RequestPathDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PluginManageActivity : CommonActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityPluginManageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityPluginManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_plugin_manage)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        val pluginRoot = File(filesDir, "plugins")
        binding.fab.setOnClickListener {
            val requestPathDialogArgs = RequestPathDialog.bundle(this)
            request(
                RequestPathDialog::class.java,
                requestPathDialogArgs
            ).response(RequestPathDialog.RequestPathResult::class.java) { result ->
                lifecycleScope.launch {
                    getFileInstance(this@PluginManageActivity, result.uri)?.let { pluginFile ->
                        lifecycleScope.launch {
                            addPlugin(pluginFile, pluginRoot)
                        }
                    }
                }
            }
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAnchorView(R.id.fab)
//                .setAction("Action", null).show()
        }
    }

    private suspend fun addPlugin(pluginFile: FileInstance, pluginRoot: File) {
        val name = pluginFile.name
        val destPluginFile = File(pluginRoot, name).ensureFile() ?: return
        withContext(Dispatchers.IO) {
            ScopeFileCopyOp(
                pluginFile,
                getFileInstance(this@PluginManageActivity, destPluginFile.toUri())!!,
                this@PluginManageActivity
            ).call()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_plugin_manage)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
