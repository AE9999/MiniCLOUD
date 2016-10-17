package com.ae.sat.servers.master.service.file;


import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by ae on 8-10-16.
 */

public interface FileService {

    void createOrUpdate(InputStream is, String name) throws IOException;

    List<String> list() throws IOException;

    byte[] get(String name) throws IOException;
}
