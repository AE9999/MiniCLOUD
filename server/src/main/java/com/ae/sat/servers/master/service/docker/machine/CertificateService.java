package com.ae.docker.machine;

import com.ae.sh.ShRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ae on 19-6-16.
 */

@Profile({"localvmDocker", "oceanvmDocker", "mixedDocker"})
@Component
public class CertificateService {

    // As suggested by Generated according to https://docs.docker.com/engine/security/https/

    private final static String CERT_PATH = "certs";

    @Value("${certificateC}")
    private String certificateC;

    @Value("${certificateST}")
    private String certificateST;

    @Value("${certificateL}")
    private String certificateL;

    @Value("${certificateO}")
    private String certificateO;

    @Value("${certificateCN}")
    private String certificateCN;

    @Autowired
    private ShRunner shRunner;

    private String getGenerateCertificatesCommand() {
        return String.format(
            "openssl genrsa -out ca-key.pem 4096 && " +
            "openssl req -new " +
                        "-x509 " +
                        "-days 365 " +
                        "-key ca-key.pem " +
                        "-sha256 " +
                        "-out ca.pem " +
                        "-subj \"/C=%s/ST=%s/L=%s/O=%s/CN=%s\" " +
            "&& " +
            "openssl genrsa -out server-key.pem 4096  && " +
            "openssl req -subj \"/CN=127.0.0.1\" -sha256 -new -key server-key.pem -out server.csr && " +
            "(echo subjectAltName = IP:127.0.0.1 > extfile.cnf) && " +
            "openssl x509 -req " +
                         "-days 365 " +
                         "-sha256 " +
                         "-in server.csr " +
                         "-CA ca.pem " +
                         "-CAkey ca-key.pem " +
                         "-CAcreateserial " +
                         "-out server-cert.pem " +
                         "-extfile extfile.cnf && " +
            "openssl genrsa -out key.pem 4096 && " +
            "openssl req -subj '/CN=client' -new -key key.pem -out client.csr && " +
            "(echo extendedKeyUsage = clientAuth > extfile.cnf) && " +
            "openssl x509 -req " +
            "-days 365 " +
            "-sha256 " +
            "-in client.csr " +
            "-CA ca.pem " +
            "-CAkey ca-key.pem " +
            "-CAcreateserial " +
            "-out cert.pem " +
            "-extfile extfile.cnf && " +
            "rm -v client.csr server.csr && " +
            "chmod -v 0400 ca-key.pem key.pem server-key.pem && " +
            "chmod -v 0444 ca.pem server-cert.pem cert.pem ;",
            certificateC,
            certificateST,
            certificateL,
            certificateO,
            certificateCN
        );
    }

    private String generateNewCertificates(String home) throws IOException {
        File homeDir = new File(home);
        if (!homeDir.canWrite()) {
            String m = "Must generate certificates, but cannot write to dir";
            throw new IllegalStateException(m);
        }
        File certDir = new File(home + File.separator + CERT_PATH);
        certDir.mkdir();
        String createCommand = String.format("cd %s && %s",
                                             certDir.getAbsolutePath(),
                                             getGenerateCertificatesCommand());
        shRunner.executeCommandAsShArgument(createCommand);
        return certDir.getAbsolutePath();
    }

    public String getCertificatesDir(String home) throws IOException {
        String certDirName = home + File.separator + CERT_PATH;
        File certDir = new File(certDirName);
        List<File> nessFiles = Arrays.asList(
                new File(certDirName + File.separator + "ca-key.pem"),
                new File(certDirName + File.separator + "ca.pem"),
                new File(certDirName + File.separator + "key.pem"),
                new File(certDirName + File.separator + "cert.pem")
        );
        if (certDir.exists()
                && nessFiles.stream().map(f -> f.exists() && f.canRead())
                .allMatch(f -> Boolean.TRUE)) {
            return certDirName;
        }
        return generateNewCertificates(home);
    }
}
