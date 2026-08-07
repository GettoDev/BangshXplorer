/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package get.gettodev.com.provider.root

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import get.gettodev.com.BuildConfig
import get.gettodev.com.app.application
import get.gettodev.com.provider.remote.IRemoteFileService
import get.gettodev.com.provider.remote.RemoteFileServiceInterface
import get.gettodev.com.provider.remote.RemoteFileSystemException
import get.gettodev.com.provider.root.isRunningAsRoot
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider
import rikka.sui.Sui
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object SuiFileServiceLauncher {
    private val initLock = Any()
    private val launchLock = Any()

    private var isSuiInitialized = false

    @Volatile
    private var binderAlive = false
    private val binderLatch = java.util.concurrent.CountDownLatch(1)

    fun ensureInitialized() {
        synchronized(initLock) {
            if (isSuiInitialized) return
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
            try {
                Sui.init(application.packageName)
            } catch (e: Throwable) {
                // Sui.init may fail, continue with Shizuku only
            }
            try {
                Shizuku.addBinderReceivedListenerSticky {
                    binderAlive = true
                    binderLatch.countDown()
                }
                Shizuku.addBinderDeadListener {
                    binderAlive = false
                }
            } catch (e: Throwable) {
                // Shizuku listeners may fail
            }
            try {
                ShizukuProvider.requestBinderForNonProviderProcess(application)
            } catch (e: Throwable) {
                // Request binder call may fail
            }
            isSuiInitialized = true
        }
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
    fun isSuiAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        ensureInitialized()

        if (Sui.isSui()) {
            return true
        }

        // 1. Check getVersion() or pingBinder()
        try {
            if (Shizuku.getVersion() >= 0) {
                binderAlive = true
                return true
            }
        } catch (e: Throwable) {}

        try {
            if (Shizuku.pingBinder()) {
                binderAlive = true
                return true
            }
        } catch (e: Throwable) {}

        if (binderAlive) {
            return true
        }

        // 2. Request binder explicitly
        try {
            ShizukuProvider.requestBinderForNonProviderProcess(application)
        } catch (e: Throwable) {}

        // 3. Wait up to 3 seconds for binder delivery
        return try {
            binderLatch.await(3, java.util.concurrent.TimeUnit.SECONDS)
            try { Shizuku.pingBinder() } catch (e: Throwable) { binderAlive }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Throws(RemoteFileSystemException::class)
    fun launchService(): IRemoteFileService {
        if (isRunningAsRoot) {
            // Service process running as root, no check needed
        } else if (!isSuiAvailable()) {
            throw RemoteFileSystemException("El servicio de Shizuku no está activo. Abre la app Shizuku e inicia el servicio.")
        }
        synchronized(launchLock) {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                val granted = try {
                    runBlocking<Boolean> {
                        suspendCancellableCoroutine<Boolean> { continuation ->
                            val listener = object : Shizuku.OnRequestPermissionResultListener {
                                override fun onRequestPermissionResult(
                                    requestCode: Int,
                                    grantResult: Int
                                ) {
                                    if (requestCode == 1001) {
                                        Shizuku.removeRequestPermissionResultListener(this)
                                        val granted = grantResult == PackageManager.PERMISSION_GRANTED
                                        if (continuation.isActive) {
                                            continuation.resume(granted)
                                        }
                                    }
                                }
                            }
                            Shizuku.addRequestPermissionResultListener(listener)
                            continuation.invokeOnCancellation {
                                Shizuku.removeRequestPermissionResultListener(listener)
                            }
                            Shizuku.requestPermission(1001)
                        }
                    }
                } catch (e: Exception) {
                    throw RemoteFileSystemException(e)
                }
                if (!granted) {
                    throw RemoteFileSystemException("Permiso de Shizuku denegado por el usuario")
                }
            }
            return try {
                runBlocking {
                    try {
                        withTimeout(RootFileService.TIMEOUT_MILLIS) {
                            suspendCancellableCoroutine { continuation ->
                                val serviceArgs = Shizuku.UserServiceArgs(
                                    ComponentName(application, SuiFileServiceInterface::class.java)
                                )
                                    .debuggable(BuildConfig.DEBUG)
                                    .daemon(false)
                                    .processNameSuffix("sui")
                                    .version(BuildConfig.VERSION_CODE)
                                val connection = object : ServiceConnection {
                                    override fun onServiceConnected(
                                        name: ComponentName,
                                        service: IBinder
                                    ) {
                                        val serviceInterface =
                                            IRemoteFileService.Stub.asInterface(service)
                                        continuation.resume(serviceInterface)
                                    }

                                    override fun onServiceDisconnected(name: ComponentName) {
                                        if (continuation.isActive) {
                                            continuation.resumeWithException(
                                                RemoteFileSystemException(
                                                    "Sui service disconnected"
                                                )
                                            )
                                        }
                                    }

                                    override fun onBindingDied(name: ComponentName) {
                                        if (continuation.isActive) {
                                            continuation.resumeWithException(
                                                RemoteFileSystemException("Sui binding died")
                                            )
                                        }
                                    }

                                    override fun onNullBinding(name: ComponentName) {
                                        if (continuation.isActive) {
                                            continuation.resumeWithException(
                                                RemoteFileSystemException("Sui binding is null")
                                            )
                                        }
                                    }
                                }
                                Shizuku.bindUserService(serviceArgs, connection)
                                continuation.invokeOnCancellation {
                                    Shizuku.unbindUserService(serviceArgs, connection, true)
                                }
                            }
                        }
                    } catch (e: TimeoutCancellationException) {
                        throw RemoteFileSystemException(e)
                    }
                }
            } catch (e: InterruptedException) {
                throw RemoteFileSystemException(e)
            }
        }
    }
}

@Keep
@RequiresApi(Build.VERSION_CODES.M)
class SuiFileServiceInterface : RemoteFileServiceInterface() {
    init {
        RootFileService.main()
    }
}
