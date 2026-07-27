package icpmapp.services;

import icpmapp.dto.responses.MediaFileResponse;
import icpmapp.entities.Media;
import icpmapp.repository.MediaRepository;
import icpmapp.services.impl.MediaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MediaServiceImplTest {

    @TempDir
    Path instanceDirectory;

    @Test
    void servesUploadedMediaFromConfiguredInstanceDirectory() throws Exception {
        MediaRepository repository = mock(MediaRepository.class);
        MediaServiceImpl service = new MediaServiceImpl(repository);
        ReflectionTestUtils.setField(service, "uploadDir", instanceDirectory.toString());
        Path mediaDirectory = Files.createDirectories(instanceDirectory.resolve("media"));
        Files.writeString(mediaDirectory.resolve("asset.svg"), "<svg/>");

        Media media = new Media();
        media.setFilename("asset.svg");
        media.setMimeType("image/svg+xml");
        when(repository.findByFilename("asset.svg")).thenReturn(Optional.of(media));

        MediaFileResponse response = service.getMediaFile("asset.svg");

        assertEquals("image/svg+xml", response.mimeType());
        assertTrue(response.resource().exists());
    }
}
