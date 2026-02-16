Place your edited Edge personalization response JSON here and rename it to:
  content_card_debug_payload.json

The JSON must have this shape:
  {
    "handle": [
      {
        "payload": [ ... ]
      }
    ]
  }

Each element in "payloads" (or "payload") is a proposition object with: id, scope, scopeDetails, items.

Ensure "scope" and any "meta.surface" in the content card data use your app's package and surface path
(e.g. mobileapp://com.adobe.marketing.mobile.messagingsample/transactions_list).

In the app: enable "Use debug payload" from the menu, then tap Refresh to load this file
instead of making a network request.
