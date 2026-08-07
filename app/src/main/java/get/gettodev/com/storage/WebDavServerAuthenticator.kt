/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.storage

import get.gettodev.com.provider.webdav.client.Authentication
import get.gettodev.com.provider.webdav.client.Authenticator
import get.gettodev.com.provider.webdav.client.Authority
import get.gettodev.com.settings.Settings
import get.gettodev.com.util.valueCompat

object WebDavServerAuthenticator : Authenticator {
    private val transientServers = mutableSetOf<WebDavServer>()

    override fun getAuthentication(authority: Authority): Authentication? {
        val server = synchronized(transientServers) {
            transientServers.find { it.authority == authority }
        } ?: Settings.STORAGES.valueCompat.find {
            it is WebDavServer && it.authority == authority
        } as WebDavServer?
        return server?.authentication
    }

    fun addTransientServer(server: WebDavServer) {
        synchronized(transientServers) { transientServers += server }
    }

    fun removeTransientServer(server: WebDavServer) {
        synchronized(transientServers) { transientServers -= server }
    }
}
