package com.adobe.marketing.mobile.messaging;

import com.adobe.marketing.mobile.AdobeCallback
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEngine
import com.adobe.marketing.mobile.launch.rulesengine.RuleConsequence

class MessagingRulesEngine(
    name: String,
    extensionApi: ExtensionApi
) : LaunchRulesEngine(
    name,
    extensionApi
) {

    fun process(event : Event) {
        val consequences : List<RuleConsequence> =  this.evaluateEvent(event)
        var eventHandled = false
        for (consequence in consequences) {
            if (consequence.type == "schema" && isReevaluable(consequence)) {
                eventHandled = true
                refreshMessagesThenProcessEvent(event)
            }
        }
        if (eventHandled.not()) {
            processEvent(event)
        }
    }

    private fun refreshMessagesThenProcessEvent(event: Event) {
        val eventData: MutableMap<String?, Any?> = HashMap()
        eventData.put("refreshmessages", true)
        val refreshMessageEvent =
            Event.Builder(
                "Refresh in-app messages",
                EventType.MESSAGING,
                EventSource.REQUEST_CONTENT
            )
                .setEventData(eventData)
                .build()

        val updateCallback = AdobeCallback<Boolean> {
            processEvent(event)
        }

        MessagingExtension.addCompletionHandler(
            CompletionHandler(
                refreshMessageEvent.uniqueIdentifier,
                updateCallback
                )
        )

        MobileCore.dispatchEvent(refreshMessageEvent)
    }

    private fun isReevaluable(consequence: RuleConsequence): Boolean {
        return true
    }
}
