package momo.kikiplus.com.kbucket;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/** Read-only access to friends and postcards stored by versions 1.0 and 2.0. */
final class Friends {
 static String archive(Context context) {
  SharedPreferences previous = context.getSharedPreferences("friends", Context.MODE_PRIVATE);
  JSONArray contacts = new JSONArray();
  JSONArray cards = new JSONArray();
  for (Map.Entry<String, ?> entry : previous.getAll().entrySet()) {
   String key = entry.getKey();
   try {
    if (key.startsWith("card.") && entry.getValue() instanceof String) {
     cards.put(new JSONObject((String) entry.getValue()));
    } else if (key.startsWith("rel.") && "friend".equals(entry.getValue())) {
     String id = key.substring(4);
     contacts.put(new JSONObject().put("id", id).put("name", previous.getString("peer." + id, "꿈친구")).put("rel", "friend"));
    }
   } catch (Exception ignored) { }
  }
  try {
   return new JSONObject().put("name", previous.getString("name", "")).put("peers", contacts).put("inbox", cards).toString();
  } catch (Exception ignored) { return "{}"; }
 }
}
