package com.ibrahim.nano_health_task.feed.data

import com.ibrahim.nano_health_task.R
import com.ibrahim.nano_health_task.feed.model.ImageMedia
import com.ibrahim.nano_health_task.feed.model.Post
import com.ibrahim.nano_health_task.feed.model.VideoMedia

object SampleFeed {
    // NOTE: Replace placeholder raw/video file with a real small mp4 in res/raw/sample_video.mp4
    val posts = listOf(
        Post(
            id = "p1",
            author = "alice",
            caption = "Beautiful sunrise",
            media = listOf(ImageMedia("m1", R.drawable.ic_launcher_foreground))
        ),
        Post(
            id = "p2",
            author = "bob",
            caption = "Short clip",
            media = listOf(VideoMedia("m2"))
        ),
        Post(
            id = "p3",
            author = "carol",
            caption = "Image + video",
            media = listOf(
                ImageMedia("m3", R.drawable.ic_launcher_foreground),
                VideoMedia("m4")
            )
        ),
        Post(
            id = "p4",
            author = "carol",
            caption = "Video",
            media = listOf(
                VideoMedia("m5")
            )
        ),
        Post(
            id = "p5",
            author = "carol",
            caption = "Video",
            media = listOf(
                VideoMedia("m6")
            )
        ),
        Post(
            id = "p6",
            author = "carol",
            caption = "Video",
            media = listOf(
                VideoMedia("m7")
            )
        ),
        Post(
            id = "p7",
            author = "carol",
            caption = "Video",
            media = listOf(
                VideoMedia("m8")
            )
        ),
        Post(
            id = "p8",
            author = "carol",
            caption = "Video",
            media = listOf(
                VideoMedia("m9")
            )
        ),
        Post(
            id = "p9",
            author = "carol",
            caption = "Video",
            media = listOf(
                VideoMedia("m10")
            )
        ),
        Post(
            id = "p10",
            author = "carol",
            caption = "Video",
            media = listOf(
                VideoMedia("m11")
            )
        )
    )
}
