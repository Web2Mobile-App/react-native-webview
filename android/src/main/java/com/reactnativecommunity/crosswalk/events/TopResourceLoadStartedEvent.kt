package com.reactnativecommunity.crosswalk.events

import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event
import com.facebook.react.uimanager.events.RCTEventEmitter

/**
 * Event emitted when loading has started
 */
class TopResourceLoadStartedEvent(viewId: Int, private val mEventData: WritableMap) :
  Event<TopResourceLoadStartedEvent>(viewId) {
  companion object {
    const val EVENT_NAME = "topResourceLoadStarted"
  }

  override fun getEventName(): String = EVENT_NAME

  override fun canCoalesce(): Boolean = false

  override fun getCoalescingKey(): Short = 0

  override fun dispatch(rctEventEmitter: RCTEventEmitter) =
    rctEventEmitter.receiveEvent(viewTag, eventName, mEventData)

}
