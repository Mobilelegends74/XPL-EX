package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XLegacyCore;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.ResultRequest;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;

public class PutHookExCommand extends CallCommandHandlerEx {
    private static final String TAG = LibUtil.generateTag(PutHookExCommand.class);

    public static final String COMMAND_NAME = "putHookExCommand";
    public static final String FIELD_DATA = "raw";
    public static final String FIELD_FLAG = "flag";
    public static final String FIELD_EXCEPTION = "exception";


    public PutHookExCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        try {
            ResultRequest res = ResultRequest.create(commandData.extras, false);
            if(!res.isValid()) {
                res.exception = "Hook Object from the Caller is Null or Invalid!";
                res.flag = false;
                return res.toBundle();
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Executing Put Hook Command on Hook [%s] Flag [%s]",
                        res.hook.getObjectId(),
                        res.flag));

            if(!DatabaseHelpEx.ensureTableIsReady_locked(XHook.TABLE_INFO, commandData.getDatabase())) {
                res.exception = "Failed to Ensure Database is Ready!";
                res.flag = false;
                Log.e(TAG, res.exception);
                return res.toBundle();
            } else {
                boolean isDelete = res.flag;
                if(!isDelete) {
                    boolean updated = DatabaseHelpEx.updateOrInsertItem(
                            commandData.getDatabase(),
                            XHook.TABLE_NAME,
                            null,
                            res.hook, true);
                    if(!updated) {
                        res.exception = Str.fm("Failed to Insert or Update [%s] to the Database!", res.hook.getObjectId());
                        res.flag = false;
                        Log.e(TAG, res.exception);
                        return res.toBundle();
                    }
                } else {
                    boolean removed = DatabaseHelpEx.deleteItem(
                            res.hook.createSnake()
                                    .table(XHook.TABLE_NAME)
                                    .asSnake()
                                    .database(commandData.getDatabase()));
                    if(!removed) {
                        res.exception = Str.fm("Failed to Remove [%] from the Database!", res.hook.getObjectId());
                        res.flag = false;
                        Log.e(TAG, res.exception);
                        return res.toBundle();
                    }
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Put Hook Command Action was Executed Successfully! Delete=%s Hook=%s",
                            res.flag,
                            res.hook.getObjectId()));

                boolean updatedCache = XLegacyCore.updateHookCache(
                        commandData.getContext(),
                        isDelete ? null : res.hook,
                        res.hook.getObjectId());
                if(!updatedCache) {
                    res.exception = Str.fm("Failed to Remove [%] from the Cache?! Was Delete [%s]", res.hook.getObjectId(), res.flag);
                    res.flag = false;
                    Log.e(TAG, res.exception);
                    return res.toBundle();
                } else {
                    res.flag = true;
                    res.hook = XLegacyCore.getHook(res.hook.getObjectId());
                    return res.toBundle();
                }
            }
        }catch (Exception e) {
            ResultRequest res = new ResultRequest();
            res.flag = false;
            res.exception = Str.fm("Failed to Execute Put Hook Command ! Error=%s  Stack=%s",
                    e.toString(),
                    RuntimeUtils.getStackTraceSafeString(e));
            Log.e(TAG, res.exception);
            return res.toBundle();
        }
    }


    public static ResultRequest putEx(Context context, XHook hook, boolean delete) {
        if(ObjectUtils.anyNull(context, hook))
            return ResultRequest.create(hook, false);
        try {
            ResultRequest req = ResultRequest.create(hook, delete);
            Bundle result = XProxyContent.luaCall(context, COMMAND_NAME, req.toBundle());
            req.fromBundle(result, delete);
            if(!req.flag)
                throw new Exception(req.exception);

            return req;
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to Update Hook [%s] Delete=%s Error=%s",
                    hook == null ? "null" : hook.getObjectId(),
                    delete,
                    e));

            return ResultRequest.create(hook, e.toString());
        }
    }
}