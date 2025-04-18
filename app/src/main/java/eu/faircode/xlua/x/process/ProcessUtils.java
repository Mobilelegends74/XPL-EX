package eu.faircode.xlua.x.process;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.utilities.StreamUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.LibUtil;

public class ProcessUtils {
    private static final String TAG = LibUtil.generateTag(ProcessUtils.class);

    public static String getProcessOutput(Process process) {
        List<String> blocks = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                blocks.add(line);
            }
        }catch (Exception e) {
            Log.e(TAG, "Error Reading Process Input! " + e);
        }

        String comm = Str.joinList(blocks, Str.NEW_LINE);
        if(DebugUtil.isDebug())
            Log.i(TAG, "Output Command Read => " + comm);

        return comm;
    }

    public static Process createProcess(final Process originalProcess, final String fakeProcessInput) {
        if(originalProcess == null) return null;
        if(DebugUtil.isDebug())
            Log.d(TAG, "Creating Fake Process Output=" + fakeProcessInput);

        try {
            return new Process() {
                @Override
                public OutputStream getOutputStream() { return originalProcess.getOutputStream(); }

                @Override
                public InputStream getInputStream() { return new ByteArrayInputStream(fakeProcessInput.getBytes()); }

                @Override
                public InputStream getErrorStream() { return originalProcess.getErrorStream(); }

                @Override
                public int waitFor() throws InterruptedException { return originalProcess.waitFor(); }

                @Override
                public int exitValue() { return originalProcess.exitValue(); }

                @Override
                public void destroy() { originalProcess.destroy(); }
            };
        }catch (Exception e) {
            Log.e(TAG, "Failed to Create Fake Process, Error=" + e);
            return null;
        }
    }
}
