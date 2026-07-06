package io.oliverapps.media3.state

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

/*
/**
 * A Composable function that safely connects to a background [MediaSessionService] or [MediaLibraryService]
 * and provides the resulting [MediaController] instance.
 *
 * This function handles the lifecycle of the MediaController, ensuring it's initialized when the
 * Composable enters the composition and released when it leaves (or on stop/dispose),
 * thus preventing memory leaks.
 *
 * @param S The type of your [MediaSessionService] subclass. This type is used internally
 * to establish the connection via a [SessionToken].
 * @param lifecycle The [Lifecycle] of the owner of this MediaController. Defaults to the lifecycle
 * of the [LocalLifecycleOwner].
 * @return A [State] object containing the [MediaController] instance (which implements the
 * [Player] interface). The value will be `null` if the controller is not yet connected or has been released.
 */
@OptIn(UnstableApi::class)
@Composable
inline fun <reified S : MediaSessionService> rememberMediaController(
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle
): State<MediaController?> {
    val appContext = LocalContext.current.applicationContext

    // Get the KClass directly from the reified type S
    val serviceKClass = S::class

    val controllerManager = remember(serviceKClass) {
        MediaControllerManager.getInstance(appContext, serviceKClass)
    }

    DisposableEffect(lifecycle, serviceKClass) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> controllerManager.initialize()
                Lifecycle.Event.ON_STOP -> controllerManager.release()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            // It's crucial to release the controller when the DisposableEffect leaves composition
            // to prevent leaks, especially if the Composable is disposed before ON_STOP.
            controllerManager.release()
        }
    }

    return controllerManager.controller
}

/**
 * A Singleton-like class that manages a [MediaController] instance for a specific
 * [MediaSessionService] subclass.
 *
 * This class implements [RememberObserver] to handle its own lifecycle within the Compose
 * composition, ensuring the [MediaController] is properly released when no longer in use.
 */
@Stable
@OptIn(UnstableApi::class)
class MediaControllerManager<S : MediaSessionService> private constructor(
    context: Context,
    private val serviceKClass: KClass<S>
) : RememberObserver {
    private val appContext = context.applicationContext
    private var factory: ListenableFuture<MediaController>? = null
    var controller = mutableStateOf<MediaController?>(null)
        private set

    init {
        initialize()
    }

    /**
     * Initializes the [MediaController].
     *
     * If the [MediaController] has not been built or has been released, this method will
     * build a new one using the provided [MediaSessionService] class.
     */
    fun initialize() {
        if (factory == null || factory?.isDone == true) {
            factory = MediaController.Builder(
                appContext,
                SessionToken(appContext, ComponentName(appContext, serviceKClass.java))
            ).buildAsync()
        }
        factory?.addListener(
            {
                controller.value = factory?.let {
                    if (it.isDone) {
                        try {
                            it.get() // Get the controller, might throw ExecutionException
                        } catch (e: Exception) {
                            print(e)
                            null
                        }
                    } else {
                        null
                    }
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    /**
     * Releases the [MediaController].
     *
     * This method will release the [MediaController] and set the controller state to `null`.
     */
    fun release() {
        factory?.let {
            if (it.isDone) {
                try {
                    MediaController.releaseFuture(it)
                } catch (e: Exception) {
                    print(e)
                }
            } else {
                MediaController.releaseFuture(it) // Release even if not done, to cancel
            }
            controller.value = null
        }
        factory = null
    }

    // Lifecycle methods for the RememberObserver interface.
    // These ensure that if the manager is "forgotten" by remember, the controller is released.
    override fun onAbandoned() { release() }
    override fun onForgotten() { release() }
    override fun onRemembered() {} // No specific action needed when remembered

    companion object {
        @Volatile
        private var instances: MutableMap<KClass<out MediaSessionService>, MediaControllerManager<*>> = mutableMapOf()

        /**
         * Returns the Singleton instance of the [MediaControllerManager] for a specific
         * [MediaSessionService] class.
         *
         * @param context The context to use when creating the [MediaControllerManager].
         * @param serviceKClass The [KClass] of the [MediaSessionService] subclass.
         * @return The Singleton instance of the [MediaControllerManager] for the given service.
         */
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
*/
