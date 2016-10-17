package com.ae.sat.servers.master.controller;

import com.ae.sat.model.Formula;
import com.ae.sat.model.SolverAssignment;
import com.ae.sat.servers.master.service.file.FileService;
import org.apache.commons.lang.StringUtils;
import org.nustaq.serialization.FSTObjectOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by ae on 7-10-16.
 */

@RestController
public class CnfController {

    @Autowired
    private FileService fileService;

    @RequestMapping(method = RequestMethod.PUT, value = "/api/cnf")
    public void upload(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        final Formula f = Formula.fromCNFStream(request.getInputStream());

        if (StringUtils.isEmpty(f.getName())) {
            f.setName(UUID.randomUUID().toString());
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        FSTObjectOutput out = new FSTObjectOutput(os);
        out.writeObject(f, Formula.class);
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

        fileService.createOrUpdate(is, f.getName());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/cnfs")
    public List<String> cnfs()
            throws IOException {
        return fileService.list();
    }
}
