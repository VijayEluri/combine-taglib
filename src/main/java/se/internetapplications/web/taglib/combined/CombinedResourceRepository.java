package se.internetapplications.web.taglib.combined;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.internetapplications.web.taglib.combined.tags.ManagedResource;

public class CombinedResourceRepository {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CombinedResourceRepository.class);

    /**
     * key(path, name) -> requestPath
     */
    private static Map<String, String> resourcePaths;
    /**
     * requestPath -> CombinedResource
     */
    private static Map<String, CombinedResource> combinedResourcePaths;

    static {
        resourcePaths = Maps.newHashMap();
        combinedResourcePaths = Maps.newHashMap();
    }

    public static boolean containsResourcePath(final String path, final String name) {
        return resourcePaths.containsKey(createResourcePathKey(path, name));
    }

    static String createResourcePathKey(final String path, final String name) {
        String directory = path.replaceAll("^/+|/+$", "");
        return String.format("%s/%s", directory.trim().length() == 0 ? "" : "/" + directory, name);
    }

    public static String getResourcePath(final String path, final String name) {
        return resourcePaths.get(createResourcePathKey(path, name));
    }

    public static CombinedResource getCombinedResource(final String requestURI) {
        return combinedResourcePaths.get(requestURI);
    }

    public static String addCombinedResource(final String path, final String name,
            final List<ManagedResource> resources, final CombineResourceStrategy combinator) {

        checkNotNull(path, "Path cannot be null.");
        checkNotNull(name, "Name cannot be null.");
        checkNotNull(resources, "Resources cannot be null.");

        String requestPath = null;

        CombinedResource resource = getCombinedResourceByKey(path, name);

        if (resource == null || resource.hasChangedFile(resources)) {
            log.info(String.format("Modified resource '%s' detected. Rebuilding...", name));
            try {

                final StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);

                long startReadAt = new Date().getTime();
                long timestamp = combinator.combineFiles(pw, resources);
                if (timestamp == 0) {
                    timestamp = startReadAt;
                }

                String contents = sw.toString();
                String md5 = Hashing.md5().hashString(contents).toString();

                requestPath = createRequestPath(path, name, md5);

                log.info("Adding combined resource" + requestPath);

                resourcePaths.put(createResourcePathKey(path, name), requestPath);
                combinedResourcePaths.put(requestPath,
                        combinator.stringToCombinedResource(contents, timestamp, md5, resources));
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            requestPath = createRequestPath(path, name, resource.getChecksum());
        }

        return requestPath;
    }

    public static long joinPaths(final PrintWriter writer, final List<ManagedResource> realPaths) throws IOException {
        log.info("Reading files");

        long timestamp = 0;
        for (ManagedResource realPath : realPaths) {

            try {
                if (realPath.isTimestampSupported()) {
                    File file = new File(realPath.getRealPath());
                    timestamp = Math.max(timestamp, file.lastModified());
                }

                String contents = CharStreams.toString(CharStreams.newReaderSupplier(realPath.getInputSupplicer(),
                        Charsets.UTF_8));

                writer.println(contents);
            } catch (IOException e) {
                throw new RuntimeException("Could not read file " + realPath, e);
            }
        }
        writer.flush();

        return timestamp;
    }

    private static long yuiCompressPaths(final PrintWriter writer, final List<String> realPaths) throws IOException,
            InterruptedException {
        YuiCompressorWriter yuiWriter = new YuiCompressorWriter(writer);

        log.info("Starting compressor thread");
        Thread compressorThread = new Thread(yuiWriter);
        compressorThread.start();

        log.info("Reading files");

        long timestamp = 0;
        for (String realPath : realPaths) {

            try {
                File file = new File(realPath);
                timestamp = Math.max(timestamp, file.lastModified());
                String contents = Files.toString(file, Charsets.UTF_8);
                yuiWriter.write(contents);
            } catch (IOException e) {
                throw new RuntimeException("Could not read file " + realPath, e);
            }
        }
        yuiWriter.flush();
        yuiWriter.close();

        compressorThread.join();
        return timestamp;
    }

    /**
     * Creates the path that will be used in the request from the browser.
     */
    static String createRequestPath(final String directory, final String id, final String checksum) {
        String path = String.format("%s-%s.combined", createResourcePathKey(directory, id), checksum);
        return path;
    }

    private static CombinedResource getCombinedResourceByKey(final String path, final String name) {
        if (containsResourcePath(path, name)) {
            String scriptPath = getResourcePath(path, name);
            return getCombinedResource(scriptPath);
        }
        return null;
    }

}
