package io.oliverapps.radio.player.state

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlin.reflect.KClass

@OptIn(UnstableApi::class)
@Composable
inline fun <reified S : MediaSessionService> rememberMediaController(
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle
): State<MediaController?> {
    val appContext = LocalContext.current.applicationContext
    val serviceKClass = S::class

    val controllerManager = remember(serviceKClass) {
        MediaControllerManager.getInstance(appContext, serviceKClass)
    }

    DisposableEffect(lifecycle, controllerManager) {
        var isInitialized = false

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (!isInitialized) {
                    controllerManager.initialize()
                    isInitialized = true
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                if (isInitialized) {
                    controllerManager.release()
                    isInitialized = false
                }
            }
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            // Only release if this specific effect sequence currently holds an active lease
            if (isInitialized) {
                controllerManager.release()
                isInitialized = false
            }
        }
    }

    return controllerManager.controller
}

/**
 * A Thread-safe, reference-counted manager that coordinates a single [MediaController]
 * instance across multiple Activities or Compose destinations.
 */
@OptIn(UnstableApi::class)
class MediaControllerManager<S : MediaSessionService> private constructor(
    context: Context,
    private val serviceKClass: KClass<S>
) {
    private val appContext = context.applicationContext
    private var factory: ListenableFuture<MediaController>? = null

    var controller = mutableStateOf<MediaController?>(null)
        private set

    private var refCount = 0

    /**
     * Increments the active reference connection claim.
     * Builds a new connection only if no underlying controller channel exists.
     */
    @Synchronized
    fun initialize() {
        refCount++

        if (factory == null) {
            val token = SessionToken(appContext, ComponentName(appContext, serviceKClass.java))
            factory = MediaController.Builder(appContext, token).buildAsync().apply {
                addListener(
                    {
                        try {
                            if (isDone) {
                                controller.value = get()
                            }
                        } catch (e: Exception) {
                            Log.e("MediaControllerManager", "Failed to resolve MediaController future", e)
                            controller.value = null
                        }
                    },
                    MoreExecutors.directExecutor()
                )
            }
        } else if (factory?.isDone == true) {
            // If another instance already connected it perfectly, immediately supply the active value
            try {
                controller.value = factory?.get()
            } catch (e: Exception) {
                controller.value = null
            }
        }
    }

    /**
     * Decrements the connection claim. Completely tears down the underlying
     * Media3 controller ONLY when the global reference count hitting zero indicates
     * no visible UI components are left using it.
     */
    @Synchronized
    fun release() {
        refCount--
        if (refCount <= 0) {
            refCount = 0 // Safeguard against underflow shifts
            factory?.let {
                try {
                    MediaController.releaseFuture(it)
                } catch (e: Exception) {
                    Log.e("MediaControllerManager", "Error freeing MediaController lease", e)
                }
                controller.value = null
            }
            factory = null
        }
    }

    companion object {
        @Volatile
        private var instances: MutableMap<KClass<out MediaSessionService>, MediaControllerManager<*>> = mutableMapOf()

        @Suppress("UNCHECKED_CAST")
        fun <S : MediaSessionService> getInstance(
            context: Context,
            serviceKClass: KClass<S>
        ): MediaControllerManager<S> {
            return instances[serviceKClass] as? MediaControllerManager<S> ?: synchronized(this) {
                (instances[serviceKClass] as? MediaControllerManager<S>) ?: MediaControllerManager(context, serviceKClass).also {
                    instances[serviceKClass] = it
                }
            }
        }
    }
}
