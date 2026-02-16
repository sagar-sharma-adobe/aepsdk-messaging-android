/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.messagingsample

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader

/**
 * Helper to load and parse a debug personalization payload from assets.
 * Expects JSON shape: { "handle": [ { "payloads": [ ... ] or "payload": [ ... ] } ] }
 */
object DebugPayloadHelper {
    private const val TAG = "DebugPayloadHelper"
    const val ASSET_FILENAME = "content_card_debug_payload.json"

    /**
     * Loads the debug payload from assets and returns the list of proposition maps,
     * or null if the file is missing or invalid.
     */
    @Suppress("UNCHECKED_CAST")
    fun loadPayloadFromAssets(assetContent: String): List<Map<String, Object>>? {
        return try {
            val json = JSONObject(assetContent)
            val handle = json.optJSONArray("handle") ?: return null
            if (handle.length() == 0) return null
            val firstHandle = handle.getJSONObject(0)
            val payloadArray = firstHandle.optJSONArray("payloads")
                ?: firstHandle.optJSONArray("payload")
                ?: return null
            jsonArrayToMapList(payloadArray)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse debug payload: ${e.message}")
            null
        }
    }

    /**
     * Reads asset file as string. Returns null if the file does not exist or cannot be read.
     */
    fun readAssetFile(stream: java.io.InputStream): String? {
        return try {
            InputStreamReader(stream).use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read asset file: ${e.message}")
            null
        }
    }

    private fun jsonObjectToMap(obj: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        for (key in obj.keys()) {
            val v = obj.get(key)
            map[key] = when (v) {
                is JSONObject -> jsonObjectToMap(v)
                is JSONArray -> jsonArrayToList(v)
                JSONObject.NULL -> null
                else -> v
            }
        }
        return map
    }

    private fun jsonArrayToList(arr: JSONArray): List<Any?> {
        val list = mutableListOf<Any?>()
        for (i in 0 until arr.length()) {
            val v = arr.get(i)
            list.add(
                when (v) {
                    is JSONObject -> jsonObjectToMap(v)
                    is JSONArray -> jsonArrayToList(v)
                    JSONObject.NULL -> null
                    else -> v
                }
            )
        }
        return list
    }

    /**
     * Converts a JSONArray of proposition objects to List<Map<String, Object>>.
     * Each element must be a JSONObject (proposition map).
     */
    private fun jsonArrayToMapList(arr: JSONArray): List<Map<String, Object>> {
        val list = mutableListOf<Map<String, Object>>()
        for (i in 0 until arr.length()) {
            val item = arr.get(i)
            if (item is JSONObject) {
                list.add(jsonObjectToMap(item) as Map<String, Object>)
            }
        }
        return list
    }
}
