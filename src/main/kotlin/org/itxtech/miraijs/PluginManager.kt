package org.itxtech.miraijs

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import org.itxtech.miraijs.`package`.PluginPackage
import java.io.File
import java.lang.Exception

object PluginManager {
    var optimizationLevel = try {
        Class.forName("android.os.Build"); 0
    } catch (e: Throwable) {
        -1
    }
    private val pluginFolder: File by lazy { File(MiraiJs.dataFolder.absolutePath + File.separatorChar + "plugins").also { it.mkdirs() } }
    val pluginData: File by lazy { File(MiraiJs.dataFolder.absolutePath + File.separatorChar + "data").also { it.mkdirs() } }

    private val plugins: HashMap<String, PluginScope> = hashMapOf()

    @OptIn(ObsoleteCoroutinesApi::class)
    private val loadPluginDispatcher = newSingleThreadContext("JsPluginLoader")
    private val loadPluginsJobs = arrayListOf<Job>()
    private val loadPluginsLock = Mutex()


    fun loadPlugins() {
        if (pluginFolder.isDirectory) {
            pluginFolder.listFiles()?.asSequence()?.forEach {
                MiraiJs.launch(loadPluginDispatcher) {
                    loadPluginsLock.withLock {
                        try {
                            val `package` = PluginPackage(it)
                            if (plugins.filter { it.key == `package`.config!!.id }.count() != 0) {
                                MiraiJs.logger.error("Conflict to load ${`package`.config!!.name}(${`package`.config!!.id}): already loaded.")
                            } else {
                                MiraiJs.logger.info("Loading ${`package`.config!!.name}.")
                                `package`.extractResources(
                                    File(pluginData.absolutePath + File.separatorChar + `package`.config!!.id)
                                )
                                val pluginScope = PluginScope(`package`)
                                pluginScope.init()
                                pluginScope.compileScripts()
                                plugins[`package`.config!!.id] = pluginScope
                            }
                        } catch (ex: Exception) {
                            MiraiJs.logger.error("Error while loading ${it.name}: $ex")
                        }
                    }
                }.also { loadPluginsJobs.add(it) }
            }
            MiraiJs.launch(loadPluginDispatcher) {
                waitLoadPluginsJobs()
                MiraiJs.logger.info("Loaded ${plugins.count()} plugins.")
            }

        } else throw RuntimeException("Plugin folder is not a folder.")
    }

    fun executePlugins() {
        plugins.forEach { (_, pluginScope) ->
            pluginScope.load()
        }
    }

    fun reloadPlugin(id: String) {
        val p = plugins[id]
        if (p != null) {
            p.reload()
        } else MiraiJs.logger.error("Plugin $id not found.")
    }

    fun unloadPlugin(id: String) {
        val p = plugins[id]
        if (p != null) {
            p.unload()
        } else MiraiJs.logger.error("Plugin $id not found.")
    }

    suspend fun waitLoadPluginsJobs() = loadPluginsJobs.joinAll()
}

@ConsoleExperimentalApi
@Suppress("unused")
object JpmCommand : CompositeCommand(
    MiraiJs, "jpm", "MiraiJs插件管理器"
) {

    @SubCommand
    @Description("Reload a plugin.")
    suspend fun CommandSender.reload(@Name("Plugin Id") id: String) {
        PluginManager.reloadPlugin(id)
    }

    @SubCommand
    @Description("Unload a plugin and disable it.")
    suspend fun CommandSender.unload(@Name("Plugin Id") id: String) {
        PluginManager.reloadPlugin(id)
    }
}