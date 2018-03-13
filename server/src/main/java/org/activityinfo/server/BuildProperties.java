package org.activityinfo.server;

import com.google.appengine.labs.repackaged.com.google.common.io.CharSource;
import com.google.appengine.labs.repackaged.com.google.common.io.Resources;
import com.google.common.base.Charsets;

import java.io.Reader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Retrieves build properties for this build.
 *
 * <p>The build.properties is generated by the <code>server/buildProperties.gradle</code>
 * build script.</p>
 */
public class BuildProperties {

    private static final Logger LOGGER = Logger.getLogger(BuildProperties.class.getName());

    public static final BuildProperties PROPERTIES = new BuildProperties();

    private String version = "dev";
    private String commitId = "dev";

    private BuildProperties() {
        try {
            CharSource source = Resources.asCharSource(Resources.getResource("build.properties"), Charsets.UTF_8);
            try(Reader reader = source.openStream()) {
                Properties properties = new Properties();
                properties.load(reader);

                version = properties.getProperty("version");
                commitId = properties.getProperty("commitId");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not read build.properties", e);
        }
    }

    public String getVersion() {
        return version;
    }

    public String getCommitId() {
        return commitId;
    }
}
