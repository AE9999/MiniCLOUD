package com.ae.sat.servers.master.controller;

/**
 * Created by ae on 21-5-16.
 * as stolen from https://spring.io/guides/gs/uploading-files/
 */
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

import com.ae.sat.model.Formula;
import com.ae.sat.servers.master.service.job.Job;
import com.ae.sat.servers.master.service.job.PlannedJobService;
import com.ae.sat.servers.master.service.job.JobService;
import com.ae.sat.servers.master.service.preprocess.PreProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FileUploadController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private JobService jobService;

    @Autowired
    private PlannedJobService plannedJobService;

    @Autowired
    private PreProcessService preProcessService;

    @Value("${maxNumberOfSolvers}")
    private int maxNumberOfSolvers;

    @Autowired
    private Executor taskExecutor;

    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String provideUploadInfo(Model model) {
        /*File rootFolder = new File(App.ROOT);
        List<String> fileNames = Arrays.stream(rootFolder.listFiles())
                .map(f -> f.getName())
                .collect(Collectors.toList());

        model.addAttribute("files",
                Arrays.stream(rootFolder.listFiles())
                        .sorted(Comparator.comparingLong(f -> -1 * f.lastModified()))
                        .map(f -> f.getName())
                        .collect(Collectors.toList())
        ); */

        model.addAttribute("files", new ArrayList<File>());

        return "uploadForm";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/cnf")
    public String handleFileUpload(@RequestParam("numberOfSolvers") final  int numberOfSolvers,
                                   @RequestParam("file") final  MultipartFile file,
                                   @RequestParam("preprocess") final boolean preprocessing,
                                   RedirectAttributes redirectAttributes) {

        if (numberOfSolvers <= 0 || numberOfSolvers > maxNumberOfSolvers) {
            String m = String.format("Amount of solvers must be between 0 and %s ..",
                                     maxNumberOfSolvers);
            redirectAttributes.addFlashAttribute("message", m);
            return "redirect:/";
        }

        if (!file.isEmpty()) {
            try {
                final Formula f = Formula.fromCNFStream(file.getInputStream());
                String m = "Succesfully uploaded a formula %s with: %s vars & %s clauses !!";
                redirectAttributes.addFlashAttribute("message",
                                                     String.format(m,
                                                     "uploaded file",
                                                     f.nVars(),
                                                     f.getClauses().size()));

                Runnable runnable = () -> {
                    try {
                        Formula formula = f;
                        if (preprocessing) {
                            log.info("Doing preprocessing ..");
                            formula = preProcessService.preprocess(f);
                            String m_ = "Preprocessing resulted into a formula " +
                                    "   with: %s vars & %s clauses !!";
                            m_ = String.format(m_, formula.nVars(),
                                                   formula.getClauses().size());
                            log.info(m_);
                        }
                        Job job = jobService.getJob(plannedJobService.simpleConfig(formula,
                                                                                   numberOfSolvers));
                        job.run(taskExecutor);

                    } catch (TimeoutException |
                             InterruptedException |
                             ExecutionException |
                            IOException e) {
                        log.error("Could not run formula", e);
                    }
                };
                taskExecutor.execute(runnable);

            }
            catch (Exception e) {
                log.error("Failed to create job", e);
                redirectAttributes.addFlashAttribute("message",
                        "You failed to upload .. => " + e.getMessage());
            }
        }
        else {
            redirectAttributes.addFlashAttribute("message",
                    "You failed to upload because the file was empty");
        }

        return "redirect:/";
    }

}
