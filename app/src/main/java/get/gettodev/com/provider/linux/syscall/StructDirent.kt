/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.provider.linux.syscall

import get.gettodev.com.provider.common.ByteString

class StructDirent(
    val d_ino: Long, /*ino_t*/
    val d_off: Long, /*off64_t*/
    val d_reclen: Int, /*unsigned short*/
    val d_type: Int, /*unsigned char*/
    val d_name: ByteString
)
