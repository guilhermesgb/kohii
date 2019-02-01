/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kohii.v1.sample.ui.sview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.fragment.app.DialogFragment
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import kohii.v1.Kohii
import kohii.v1.Playback
import kohii.v1.sample.R
import kohii.v1.sample.common.BaseFragment
import kohii.v1.sample.ui.player.InitData
import kohii.v1.sample.ui.player.PlayerDialogFragment
import kotlinx.android.synthetic.main.fragment_scroll_view.playerContainer
import kotlinx.android.synthetic.main.fragment_scroll_view.playerView

@Keep
class ScrollViewFragment : BaseFragment(),
    Playback.Callback,
    PlayerDialogFragment.Callback {

  companion object {
    const val videoUrl =
    // http://www.caminandes.com/download/03_caminandes_llamigos_1080p.mp4
      "https://video-dev.github.io/streams/x36xhzz/x36xhzz.m3u8" // Big Buck Bunny

    fun newInstance() = ScrollViewFragment().also {
      it.arguments = Bundle()
    }
  }

  private val videoTag by lazy { "${javaClass.canonicalName}::$videoUrl" }
  private var playback: Playback<PlayerView>? = null
  private var dialogPlayer: DialogFragment? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val viewRes = R.layout.fragment_scroll_view
    return inflater.inflate(viewRes, container, false)
  }

  @Suppress("RedundantOverride")
  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    // ⬇︎ For demo of manual fullscreen.
    // requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    playback = Kohii[this].setUp(videoUrl)
        .copy(repeatMode = Player.REPEAT_MODE_ONE)
        .copy(tag = videoTag)
        .asPlayable()
        .bind(playerView)
        .also {
          it.addCallback(this@ScrollViewFragment)
          it.observe(viewLifecycleOwner)
        }

    view?.run {
      playerContainer.setOnClickListener {
        dialogPlayer = PlayerDialogFragment.newInstance(
            videoTag,
            InitData(tag = videoTag, aspectRatio = 16 / 9f)
        )
        dialogPlayer!!.show(childFragmentManager, videoTag)
      }
    }
  }

  override fun onStop() {
    super.onStop()
    playerContainer.setOnClickListener(null)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    playback?.removeCallback(this)
  }

  // BEGIN: Playback.Callback

  override fun onActive(
    playback: Playback<*>,
    target: Any?
  ) {
    startPostponedEnterTransition()
  }

  override fun onInActive(
    playback: Playback<*>,
    target: Any?
  ) = Unit

  // END: Playback.Callback

  // BEGIN: PlayerDialogFragment.Callback

  override fun onDialogActive(tag: Any) {
  }

  override fun onDialogInActive(tag: Any) {
    Kohii[this].findPlayable(tag)
        ?.bind(playerView)
  }

  // END: PlayerDialogFragment.Callback
}
