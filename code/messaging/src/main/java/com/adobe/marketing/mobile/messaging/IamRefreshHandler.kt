/*
  Copyright 2026 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.messaging

import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.AdobeCallback
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.messaging.MessagingConstants.EventDataKeys.Messaging.REFRESH_MESSAGES

internal object IamRefreshHandler {

    private val callbacks = ArrayList<AdobeCallback<Boolean>>()
    private var updateInProgress = false

    /**
     * Refreshes in-app messages and notifies the [callback] when the update is complete.
     * If an update is already in progress, the [callback] is queued and will be notified when the
     * current update completes.
     *
     * @param callback the callback to be notified when the update is complete
     */
    fun refreshInAppMessages(
        callback: AdobeCallback<Boolean>? = null
    ) {
        synchronized(callbacks) {
            if (callback != null) {
                callbacks.add(callback)
            }
            if (updateInProgress) {
                return
            }
            updateInProgress = true
        }
        requestIamUpdate()
    }

    private fun requestIamUpdate() {
        val eventData: MutableMap<String?, Any?> = HashMap()
        eventData[REFRESH_MESSAGES] = true
        val refreshMessageEvent =
            Event.Builder(
                "Refresh in-app messages",
                EventType.MESSAGING,
                EventSource.REQUEST_CONTENT
            )
                .setEventData(eventData)
                .build()

        MessagingExtension.addCompletionHandler(
            CompletionHandler(
                refreshMessageEvent.uniqueIdentifier
            ) { success ->
                val callbacksToNotify: List<AdobeCallback<Boolean>>
                synchronized(callbacks) {
                    updateInProgress = false
                    callbacksToNotify = ArrayList(callbacks)
                    callbacks.clear()
                }

                for (callback in callbacksToNotify) {
                    callback.call(success)
                }
            }
        )

        MobileCore.dispatchEvent(refreshMessageEvent)
    }

    /**
     * Resets the handler state.
     * Used for testing purposes.
     */
    @VisibleForTesting
    fun reset() {
        synchronized(callbacks) {
            callbacks.clear()
            updateInProgress = false
        }
    }
}
