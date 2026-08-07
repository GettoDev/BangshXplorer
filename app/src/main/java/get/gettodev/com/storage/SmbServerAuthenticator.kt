/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.storage

import get.gettodev.com.provider.smb.client.Authenticator
import get.gettodev.com.provider.smb.client.Authority
import get.gettodev.com.settings.Settings
import get.gettodev.com.util.valueCompat

object SmbServerAuthenticator : Authenticator {
    private val transientServers = mutableSetOf<SmbServer>()

    override fun getPassword(authority: Authority): String? {
        val server = synchronized(transientServers) {
            transientServers.find { it.authority == authority }
        } ?: Settings.STORAGES.valueCompat.find {
            it is SmbServer && it.authority == authority
        } as SmbServer?
        return server?.password
    }

    fun addTransientServer(server: SmbServer) {
        synchronized(transientServers) { transientServers += server }
    }

    fun removeTransientServer(server: SmbServer) {
        synchronized(transientServers) { transientServers -= server }
    }
}
