package com.ibrahim.nano_health_task.feed.video

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import java.util.*

/**
 * Simple LRU player pool to reduce allocation overhead and improve smooth playback.
 * Keeps up to [maxPlayers] ExoPlayer instances and reuses idle ones.
 */
object PlayerPool {
    private lateinit var appContext: Context
    private data class PooledPlayer(val player: ExoPlayer, var tag: String?)

    private val pool: LinkedList<PooledPlayer> = LinkedList()
    private val maxPlayers = 2

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    @Synchronized
    fun getPlayerFor(tag: String, videoResId: Int): ExoPlayer {
        // If a player is already bound to this tag, return it
        val existing = pool.find { it.tag == tag }
        if (existing != null) {
            // move to front (recently used)
            pool.remove(existing)
            pool.addFirst(existing)
            return existing.player
        }

        // Try to find an idle player
        val idle = pool.find { it.tag == null }
        val pooled = if (idle != null) {
            idle
        } else {
            // Create new if under max, otherwise evict least recently used
            if (pool.size < maxPlayers) {
                val player = ExoPlayer.Builder(appContext).build()
                val p = PooledPlayer(player, null)
                pool.addFirst(p)
                p
            } else {
                // evict last and reuse
                val removed = pool.removeLast()
                removed.player.stop()
                removed.player.clearMediaItems()
                removed.tag = null
                pool.addFirst(removed)
                removed
            }
        }

        pooled.tag = tag
        // bind media
        val uri = Uri.parse("android.resource://${appContext.packageName}/$videoResId")
        val mediaItem = MediaItem.fromUri(uri)
        pooled.player.setMediaItem(mediaItem)
        pooled.player.prepare()
        return pooled.player
    }

    @Synchronized
    fun play(tag: String) {
        pool.find { it.tag == tag }?.player?.play()
    }

    @Synchronized
    fun pause(tag: String) {
        pool.find { it.tag == tag }?.player?.pause()
    }

    @Synchronized
    fun unbind(tag: String) {
        val p = pool.find { it.tag == tag }
        if (p != null) {
            p.player.stop()
            p.player.clearMediaItems()
            p.tag = null
        }
    }

    @Synchronized
    fun releaseAll() {
        pool.forEach { it.player.release() }
        pool.clear()
    }
}

