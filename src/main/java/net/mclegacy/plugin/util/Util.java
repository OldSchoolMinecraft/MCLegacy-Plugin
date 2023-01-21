package net.mclegacy.plugin.util;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util
{
    public static String generateToken()
    {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String postJSON(String url, String payload)
    {
        try
        {
            URL object = new URL(url);

            HttpURLConnection con = (HttpURLConnection) object.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestMethod("POST");

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(payload);
            wr.flush();

            StringBuilder sb = new StringBuilder();
            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK)
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
                String line = null;
                while ((line = br.readLine()) != null)
                    sb.append(line).append("\n");
                br.close();
                if (Debugger.isEnabled())
                {
                    System.out.println("POST: " + url);
                    System.out.println("Response Code: " + HttpResult);
                    System.out.println("Response Raw: " + sb);
                }
                return sb.toString();
            } else return "{}";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "{}";
        }
    }

    public static String get(String url)
    {
        try
        {
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;

            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();
            if (Debugger.isEnabled() && con.getResponseCode() != 200)
            {
                System.out.println("GET: " + url);
                System.out.println("Response Code: " + con.getResponseCode());
                System.out.println("Response Raw: " + response);
            }
            return response.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "";
    }

    public static long parseDateDiff(final String time, final boolean future) throws Exception
    {
        final Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?(?:([0-9]+)\\s*(?:s[a-z]*)?)?", 2);
        final Matcher m = timePattern.matcher(time);
        int years = 0;
        int months = 0;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        boolean found = false;
        while (m.find())
        {
            if (m.group() != null)
            {
                if (m.group().isEmpty())
                    continue;
                for (int i = 0; i < m.groupCount(); ++i)
                {
                    if (m.group(i) != null && !m.group(i).isEmpty())
                    {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    continue;
                if (m.group(1) != null && !m.group(1).isEmpty())
                    years = Integer.parseInt(m.group(1));
                if (m.group(2) != null && !m.group(2).isEmpty())
                    months = Integer.parseInt(m.group(2));
                if (m.group(3) != null && !m.group(3).isEmpty())
                    weeks = Integer.parseInt(m.group(3));
                if (m.group(4) != null && !m.group(4).isEmpty())
                    days = Integer.parseInt(m.group(4));
                if (m.group(5) != null && !m.group(5).isEmpty())
                    hours = Integer.parseInt(m.group(5));
                if (m.group(6) != null && !m.group(6).isEmpty())
                    minutes = Integer.parseInt(m.group(6));
                if (m.group(7) != null && !m.group(7).isEmpty()) {
                    seconds = Integer.parseInt(m.group(7));
                    break;
                }
                break;
            }
        }
        if (!found)
            throw new Exception("illegalDate");
        final Calendar c = new GregorianCalendar();
        if (years > 0)
            c.add(1, years * (future ? 1 : -1));
        if (months > 0)
            c.add(2, months * (future ? 1 : -1));
        if (weeks > 0)
            c.add(3, weeks * (future ? 1 : -1));
        if (days > 0)
            c.add(5, days * (future ? 1 : -1));
        if (hours > 0)
            c.add(11, hours * (future ? 1 : -1));
        if (minutes > 0)
            c.add(12, minutes * (future ? 1 : -1));
        if (seconds > 0)
            c.add(13, seconds * (future ? 1 : -1));
        return c.getTimeInMillis() / 1000L; // divide by 1000L to get unixtime
    }

    public static String formatUnixTime(long timestamp)
    {
        Date date = new Date(timestamp*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a z");
        sdf.setTimeZone(TimeZone.getTimeZone("PST"));
        return sdf.format(date);
    }

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate)
    {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++)
        {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1)
            {
                b[i] = '\u00A7';
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
    }
}
