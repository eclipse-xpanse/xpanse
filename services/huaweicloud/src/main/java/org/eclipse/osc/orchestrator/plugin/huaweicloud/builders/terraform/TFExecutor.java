package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions.TFExecutorException;
import org.eclipse.osc.services.ocl.loader.Ocl;

@Slf4j
public class TFExecutor {

    private final Ocl ocl;
    private final Map<String, String> env;
    private String tfPath;
    private String workPath;

    public TFExecutor(final Ocl ocl, Map<String, String> env) {
        this.ocl = ocl;
        this.env = env;
    }

    public void createWorkspace() {
        File ws =
            new File("terraform_ws" + FileSystems.getDefault().getSeparator() + ocl.getName());

        if (!ws.exists() && !ws.mkdirs()) {
            throw new TFExecutorException(
                "Create workspace for TFExecutor failed, File path not created: "
                    + ws.getAbsolutePath());
        }

        tfPath = ws.getAbsolutePath() + FileSystems.getDefault().getSeparator() + "main.tf";
        String verPath =
            ws.getAbsolutePath() + FileSystems.getDefault().getSeparator() + "version.tf";
        workPath = ws.getAbsolutePath();
        log.info("Terraform working directory is " + workPath);

        try {
            try (FileWriter versionFile = new FileWriter(verPath)) {
                versionFile.write(""
                    + "terraform {\n"
                    + "  required_providers {\n"
                    + "    huaweicloud = {\n"
                    + "      source = \"huaweicloud/huaweicloud\"\n"
                    + "      version = \">= 1.20.0\"\n"
                    + "    }\n"
                    + "  }\n"
                    + "}");
            }
        } catch (IOException ex) {
            log.error("Create terraform version file failed.", ex);
            throw new TFExecutorException("Create terraform version file failed.", ex);
        }
    }

    public void createTFScript() {
        Ocl2Hcl hcl = new Ocl2Hcl(ocl);
        String hclStr = hcl.getHcl();

        try {
            try (FileWriter tfFile = new FileWriter(tfPath)) {
                tfFile.write(hclStr);
            }
        } catch (IOException ex) {
            throw new TFExecutorException("TFExecutor createTFScript failed.", ex);
        }
    }

    private boolean execute(String[] cmd, StringBuilder stdOut) {
        log.info("Will executing cmd: " + String.join(" ", cmd));
        try {
            String line;
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            processBuilder.environment().putAll(this.env);
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(new File(workPath));
            Process process = processBuilder.start();
            BufferedReader outputReader =
                new BufferedReader(new InputStreamReader((process.getInputStream())));
            while ((line = outputReader.readLine()) != null) {
                stdOut.append(line).append("\n");
            }

            process.waitFor();
            if (process.exitValue() != 0) {
                log.error("TFExecutor execute finished with abnormal value.");
                return false;
            }
        } catch (final IOException ex) {
            throw new TFExecutorException(Arrays.toString(cmd), stdOut.toString(), ex);
        } catch (final InterruptedException ex) {
            log.error("TFExecutor execute be interrupted.");
            Thread.currentThread().interrupt();
        }

        return true;
    }

    public boolean tfInit() {
        String[] cmd = {"terraform", "init"};
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute(cmd, out);
        log.info(out.toString());
        return exeRet;
    }

    public boolean tfPlan() {
        // TODO: Dynamic variables need to be supported.
        String[] cmd = {"terraform", "plan"};
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute(cmd, out);
        log.info(out.toString());
        return exeRet;
    }

    public boolean tfApply() {
        // TODO: Dynamic variables need to be supported.
        String[] cmd = {"terraform", "apply", "-auto-approve"};
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute(cmd, out);
        log.info(out.toString());
        return exeRet;
    }

    public boolean tfDestroy() {
        // TODO: Dynamic variables need to be supported.
        String[] cmd = {"terraform", "destroy", "-auto-approve"};
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute(cmd, out);
        log.info(out.toString());
        return exeRet;
    }
}
