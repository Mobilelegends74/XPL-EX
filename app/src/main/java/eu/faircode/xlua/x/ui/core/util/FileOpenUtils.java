package eu.faircode.xlua.x.ui.core.util;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.xstandard.interfaces.IJsonSerial;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.interfaces.IValidator;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHookIO;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.LibUtil;

public class FileOpenUtils {
    private static final String TAG = LibUtil.generateTag(FileOpenUtils.class);


    public static <T extends IJsonSerial> boolean writeJsonElementToUri(
            Context context,
            Uri uri,
            T item) {
        try {
            Log.d(TAG, Str.fm("Writing Item [%s] JSON Data to URI [%s]",
                    Str.toObjectId(item),
                    Str.toStringOrNull(uri)));

            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "w");
            if (pfd == null) return false;
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(item.toJSON().getBytes(StandardCharsets.UTF_8));
            fileOutputStream.close();
            pfd.close();
            return true;
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed Writing Items [%s] to URI [%s] Error=%s",
                    Str.toObjectId(item),
                    Str.toStringOrNull(uri),
                    e));

            return false;
        }
    }


    public static <T extends IJsonSerial> List<T> readJsonElementsFromUri(
            @NonNull Context context,
            @NonNull Uri uri,
            String arrayFieldName,
            Class<T> clazz) {
        List<T> items = new ArrayList<>();
        try {
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Reading File as Json from the Open File Dialog..."));

            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null)
                return items;

            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) stringBuilder.append(line);
            inputStream.close();
            String data = stringBuilder.toString();
            if(Str.isEmpty(data))
                return items;

            try {
                JSONArray array = new JSONArray(data);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Reading as a JSON Array: " + ListUtil.size(array));

                if(ArrayUtils.isValid(array)) {
                    for (int i = 0; i < array.length(); i++) {
                        try {
                            JSONObject obj = array.getJSONObject(i);
                            T instance = clazz.newInstance();
                            instance.fromJSONObject(obj);

                            if(instance instanceof IValidator) {
                                IValidator iObj = (IValidator) instance;
                                if(!iObj.isValid())
                                    continue;
                            }

                            items.add(instance);
                        } catch (Exception ignored) { }
                    }
                }

                if(!items.isEmpty())
                    return items;
            }catch (Exception ignored) { }


            try {
                JSONObject obj = new JSONObject(data);
                if(obj.has(arrayFieldName)) {
                    JSONArray array = obj.optJSONArray(arrayFieldName);
                    if(DebugUtil.isDebug())
                           Log.d(TAG, Str.fm("Reading as JSON Array Field [%s] with Count %s",
                                   arrayFieldName,
                                   ListUtil.size(array)));

                    if(ArrayUtils.isValid(array)) {
                        for(int i = 0; i < array.length(); i++) {
                            try {
                                JSONObject subElement = array.getJSONObject(i);
                                T instance = clazz.newInstance();
                                instance.fromJSONObject(subElement);

                                if(instance instanceof IValidator) {
                                    IValidator iObj = (IValidator) instance;
                                    if(!iObj.isValid())
                                        continue;
                                }

                                items.add(instance);
                            }catch (Exception ignored) { }
                        }
                    }

                    if(!items.isEmpty())
                        return items;
                }

            }catch (Exception ignored) { }

            JSONObject obj = new JSONObject(data);
            T instance = clazz.newInstance();
            instance.fromJSONObject(obj);

            if(instance instanceof IValidator) {
                IValidator iObj = (IValidator) instance;
                if(iObj.isValid()) {
                    items.add(instance);
                }
            }

            return items;
        } catch (Exception e) {
            Log.e(TAG, Str.fm("Error Reading JSON File! Count=%s Error=%s",
                    items.size(),
                    e));
            return items;
        } finally {
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Finished Reading JSON From URI [%s] Items Returning Count [%s] From Clazz [%s] Array Field Name [%s]",
                        Str.toStringOrNull(uri),
                        items.size(),
                        Str.toObjectClassName(clazz),
                        arrayFieldName));
        }
    }
}
