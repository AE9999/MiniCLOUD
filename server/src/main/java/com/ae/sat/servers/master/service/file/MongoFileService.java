package com.ae.sat.servers.master.service.file;

import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by ae on 8-10-16.
 */

@Controller
public class MongoFileService implements FileService {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Override
    public void createOrUpdate(InputStream is, String name) throws IOException {
        Optional<GridFSDBFile> existing = maybeLoadFile(name);
        if (existing.isPresent()) {
            gridFsTemplate.delete(getFilenameQuery(name));
        }
        gridFsTemplate.store(is, name).save();
    }

    @Override
    public List<String> list() throws IOException {
        return getFiles().stream()
                         .map(GridFSDBFile::getFilename)
                         .collect(Collectors.toList());
    }

    @Override
    public byte[] get(String name) throws IOException {
        Optional<GridFSDBFile> optionalCreated = maybeLoadFile(name);
        if (optionalCreated.isPresent()) {
            GridFSDBFile created = optionalCreated.get();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            created.writeTo(os);
            return os.toByteArray();
        } else {
            return null;
        }
    }

    private List<GridFSDBFile> getFiles() {
        return gridFsTemplate.find(null);
    }

    private Optional<GridFSDBFile> maybeLoadFile(String name) {
        GridFSDBFile file = gridFsTemplate.findOne(getFilenameQuery(name));
        return Optional.ofNullable(file);
    }

    private static Query getFilenameQuery(String name) {
        return Query.query(GridFsCriteria.whereFilename().is(name));
    }
}
