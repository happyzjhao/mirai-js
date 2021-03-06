/*
 *
 * Mirai Js
 *
 * Copyright (C) 2020 iTX Technologies
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author PeratX
 * @website https://github.com/iTXTech/mirai-js
 *
 */

package org.itxtech.miraijs

import kotlinx.coroutines.launch
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.extensions.PluginLoaderProvider
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import org.itxtech.miraijs.libs.PrimitivePluginData

object MiraiJs : KotlinPlugin(
    JvmPluginDescriptionBuilder("org.itxtech.miraijs.MiraiJs", "2.0-RC")
        .id("org.itxtech.miraijs")
        .info("强大的 Mirai JavaScript 插件运行时。")
        .author("iTX Technologies")
        .build()
) {

    @ConsoleExperimentalApi
    override fun PluginComponentStorage.onLoad() {
        JpmCommand.register()
        PluginManager.loadPlugins()
    }

    override fun onEnable() {
        MiraiJs.launch {
            PluginManager.waitLoadPluginsJobs()
            PluginManager.executePlugins()
        }
    }

    fun <T> withConsolePluginContext(block: KotlinPlugin.() -> T) = block(this)

    override fun onDisable() {

    }
}
