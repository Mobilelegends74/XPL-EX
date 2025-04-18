package eu.faircode.xlua.interceptors.shell.handlers;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.xstandard.interfaces.ICommandIntercept;
import eu.faircode.xlua.interceptors.UserContextMaps;
import eu.faircode.xlua.interceptors.shell.CommandInterceptor;
import eu.faircode.xlua.interceptors.shell.ShellInterception;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.hook.interceptors.file.StatCleaner;
import eu.faircode.xlua.x.hook.interceptors.file.cleaners.StatUtils;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.LibUtil;

public class StatIntercept extends CommandInterceptor implements ICommandIntercept {
    private static final String TAG = LibUtil.generateTag(StatIntercept.class);

    private static final String STAT_INTERCEPT_SETTING = "intercept.shell.stat.bool";

    @SuppressWarnings("unused")
    public StatIntercept() { this.command = "stat"; this.setting = STAT_INTERCEPT_SETTING; }

    @Override
    public boolean isCommand(ShellInterception results) { return results.hasCommand("stat"); }

    @Override
    public boolean interceptCommand(ShellInterception result) {
        if(result != null && result.isValid) {
            UserContextMaps maps = result.getUserMaps();
            if(maps != null) {
                if(!keepGoing(maps, STAT_INTERCEPT_SETTING))
                    return true;

                final String output = result.getCommandOutput();
                if(Str.isEmpty(output)) {
                    Log.e(TAG, "STAT Will not be intercepted, Output is Empty! Args=(" + result.getCommandLine() + ") Output=" + output);
                    return false;
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, "STAT Output=" + output);

                String file = StatUtils.extractFilePath(output);
                if(Str.isEmpty(file)) {
                    if(ArrayUtils.isValid(result.commandLine)) {
                        int ix = Str.firstItemThatEndsWithInListIndex(Arrays.asList(result.commandLine), "stat", true, -1);
                        if(ix == -1) {
                            Log.w(TAG, "Failed to extract File from Command=(" + Str.joinArray(result.commandLine) + ") Output=(" + result.getCommandOutput() + ")");
                            return false;
                        }

                        if(result.commandLine.length - ix + 1 > 0) {
                            for(int i = ix; i < result.commandLine.length; i++) {
                                String com = result.commandLine[i];
                                if(com == null)
                                    continue;

                                if(com.startsWith("-") || com.startsWith("%"))
                                    continue;

                                if(com.startsWith(File.separator) && com.length() > 1) {
                                    file = com.trim();
                                    break;
                                }
                            }

                            if(Str.isEmpty(file))
                                file = result.param.getPackageName();

                        } else {
                            file = result.param.getPackageName();
                        }
                    } else {
                        Log.w(TAG, "Command Line Array is In Valid!");
                        return false;
                    }
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, "STAT Extracted File=(" + file + ") Command Line=(" + Str.joinArray(result.commandLine) + ") Output=(" + result.getCommandLine() + ")");

                if(Str.isEmpty(file)) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Failed to Extract File Args=(" + Str.joinArray(result.commandLine) + ") Result=" + Str.ensureNoDoubleNewLines(result.getCommandOutput()));

                    return false;
                }

                return StatCleaner.cleanStatCommand(file, output, result);
            } else {
                Log.e(TAG, "Maps for [stat] Command interception is NULL! Stack=" + RuntimeUtils.getStackTraceSafeString(new Throwable()));
            }
        } else {
            Log.e(TAG, "Result for Command [stat] Interception is Invalid Some how! Stack=" + RuntimeUtils.getStackTraceSafeString(new Throwable()));
        }
        return false;
    }
}
