package io.oliverapps.radio.player.state

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
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

/**
 * A Composable function that provides a MediaController instance for a given MediaSessionService.
 *
 * This function handles the lifecycle of the MediaController, ensuring it's initialized when the
 * Composable enters the composition and released when it leaves, or when the associated lifecycle
 * event occurs.
 *
 * @param S The type of your [MediaSessionService] subclass.
 * @param mediaSessionService The [KClass] of your [MediaSessionService] subclass. This is required to
 * construct the [ComponentName] for the [SessionToken].
 * @param lifecycle The [Lifecycle] of the owner of this MediaController. Defaults to the lifecycle
 * of the [LocalLifecycleOwner].
 * @return A [State] object containing the [MediaController] instance. The Composable will
 * automatically re-compose whenever the state changes. The value will be `null` if the
 * controller is not yet connected or has been released.
 * NOTE: MediaController is under the hood just an PLayer(exoplayer) so you have access to the same methods as you normally would in that case
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
                            // Handle potential errors during controller retrieval
                            // e.g., service not found, permission issues.
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
                    // Log or handle the exception if getting the controller to release it fails
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
