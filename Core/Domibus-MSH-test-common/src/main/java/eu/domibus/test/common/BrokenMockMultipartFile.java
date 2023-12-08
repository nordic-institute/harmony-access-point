package eu.domibus.test.common;

import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;

public class BrokenMockMultipartFile extends MockMultipartFile {
    public BrokenMockMultipartFile(String name, byte[] content) {
        super(name, content);
    }

    @Override
    public byte[] getBytes() throws IOException {
        throw  new IOException("Cannot read bytes.");
    }
}