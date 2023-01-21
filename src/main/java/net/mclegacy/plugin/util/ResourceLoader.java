package net.mclegacy.plugin.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author moderator_man
 * @since 1.0
 */
public class ResourceLoader
{
    private String resourceLocation;
    private String basePath;
    private int lastReadBytesTotal = 0;
    private boolean exists;
    private boolean internal = false;
    private BasicFileAttributes attr;
    private String preLoadContent;

    public ResourceLoader() {}

    public ResourceLoader(String resourceLocation)
    {
        this.resourceLocation = resourceLocation;
        this.basePath = "content/";
        this.exists = new File(basePath + resourceLocation).exists();
        if (!exists) return;
        try
        {
            this.attr = Files.readAttributes(Paths.get(new File(basePath + resourceLocation).getAbsolutePath()), BasicFileAttributes.class);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public ResourceLoader(String resourceLocation, String basePath)
    {
        this(resourceLocation);
        this.basePath = basePath;
    }

    public ResourceLoader useInternal()
    {
        internal = true;
        basePath = "/" + basePath;
        return this;
    }

    public ResourceLoader preLoad()
    {
        if (preLoadContent != null) return this; // do not preload twice
        preLoadContent = read();
        return this;
    }

    public BasicFileAttributes getAttributes()
    {
        return attr;
    }

    public String getPreLoadContent()
    {
        return preLoadContent;
    }

    public void injectResource(String key, ResourceLoader resource)
    {
        injectContent(key, resource.read());
    }

    public void injectContent(String key, String content)
    {
        if (preLoadContent == null)
            throw new RuntimeException("You must pre-load the content before you inject content!");
        preLoadContent = preLoadContent.replace(String.format("${%s}", key), content);
    }

    public boolean isGood()
    {
        return  exists &&
                attr != null &&
                resourceLocation != null &&
                basePath != null &&
                !resourceLocation.isEmpty() &&
                !basePath.isEmpty();
    }

    public InputStream getInputStream()
    {
        return getClass().getResourceAsStream(basePath + resourceLocation);
    }

    public FileInputStream getFileInputStream()
    {
        try
        {
            return new FileInputStream(basePath + resourceLocation);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String read()
    {
        return readAsString(internal ? getInputStream() : getFileInputStream());
    }

    private String readAsString(InputStream is)
    {
        if (is == null) return "readAsString() is == null"; // sanity check
        lastReadBytesTotal = 0;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is)))
        {
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) > 0)
            {
                String readData = String.valueOf(buf, 0, numRead);
                sb.append(readData);
                lastReadBytesTotal += numRead;
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public boolean directTo(InputStream is, Writer writer)
    {
        if (is == null) return false; // sanity check
        lastReadBytesTotal = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is)))
        {
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) > 0)
            {
                String readData = String.valueOf(buf, 0, numRead);
                writer.append(readData);
                lastReadBytesTotal += numRead;
            }
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getLastReadBytesTotal()
    {
        return lastReadBytesTotal;
    }

    @Override
    public String toString()
    {
        return resourceLocation;
    }
}