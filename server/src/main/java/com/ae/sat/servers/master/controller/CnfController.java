package com.ae.sat.servers.master.controller;

import com.ae.sat.model.Formula;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ae on 7-10-16.
 */

@RestController
public class CnfController {

    @RequestMapping(method = RequestMethod.PUT, value = "/api/cnf")
    public void upload(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        final Formula f = Formula.fromCNFStream(request.getInputStream());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/cnfs")
    public List<String> cnfs()
            throws IOException {
        return new ArrayList<>();
    }
}
