/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.provider.ftp.client

interface Authenticator {
    fun getPassword(authority: Authority): String?
}
