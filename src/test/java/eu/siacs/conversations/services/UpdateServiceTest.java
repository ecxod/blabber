package eu.siacs.conversations.services;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UpdateServiceTest {

    @Test
    public void detectsNewerGitHubRelease() {
        assertEquals(1, UpdateService.compareVersions("v3.1.6", "3.1.5"));
        assertEquals(1, UpdateService.compareVersions("3.10.0", "3.9.9"));
        assertEquals(1, UpdateService.compareVersions("3.1.5.1", "3.1.5"));
    }

    @Test
    public void ignoresCurrentAndOlderReleases() {
        assertEquals(0, UpdateService.compareVersions("v3.1.5", "3.1.5"));
        assertEquals(-1, UpdateService.compareVersions("3.1.4", "3.1.5"));
    }
}
