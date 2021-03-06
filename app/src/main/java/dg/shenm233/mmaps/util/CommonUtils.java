/*
 * Copyright 2016 Shen Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dg.shenm233.mmaps.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.DecimalFormat;

import dg.shenm233.mmaps.MainApplication;
import dg.shenm233.mmaps.R;

public class CommonUtils {
    public static void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 将秒转换为时:分的形式
     */
    public static String getFriendlyDuration(long sec) {
        Context context = MainApplication.getAppContext();
        StringBuilder sb = new StringBuilder();
        if (sec <= 60)
            return sb.append(1).append(context.getText(R.string.minute))
                    .toString();
        else if (sec <= 3600)
            return sb.append(sec / 60).append(context.getText(R.string.minute))
                    .toString();
        else {
            return sb.append(sec / 3600).append(context.getText(R.string.hour)).append(" ")
                    .append((sec % 3600) / 60).append(context.getText(R.string.minute))
                    .toString();
        }
    }

    /**
     * 将米数转换成更友好的样式
     */
    public static String getFriendlyLength(int lenMeter) {
        if (lenMeter > 10000) // 10 km
        {
            int dis = lenMeter / 1000;
            return dis + ChString.Kilometer;
        }

        if (lenMeter > 1000) {
            float dis = (float) lenMeter / 1000;
            DecimalFormat fnum = new DecimalFormat("##0.0");
            String dstr = fnum.format(dis);
            return dstr + ChString.Kilometer;
        }

        return lenMeter + ChString.Meter;
    }

    /**
     * 添加“元”单位
     */
    public static String getFriendlyCost(float yuan) {
        return yuan + "元";
    }

    public static String getFriendlyBytes(long bytes) {
        if (bytes > 1000000) {
            return String.format("%.2fMB", (double) bytes / 1000000);
        } else if (bytes > 1000) {
            return String.format("%.2fKB", (double) bytes / 1000);
        } else {
            return String.format("%dB", bytes);
        }
    }

    public static boolean isStringEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static void writeToFile(String s, String filePath, boolean append) {
        File f = new File(filePath);
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(f, append));
            writer.write(s);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getStringFromFile(String filePath) {
        String s = null;
        File f = new File(filePath);
        Reader reader = null;
        try {
            reader = new BufferedReader(new FileReader(f));
            StringBuilder sb = new StringBuilder();
            char[] buff = new char[50];
            int length;
            while ((length = reader.read(buff)) != -1) {
                sb.append(buff, 0, length);
            }
            s = sb.toString();
        } catch (FileNotFoundException e) {
            s = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }
}
