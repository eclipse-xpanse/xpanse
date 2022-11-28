package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.utils.SystemCmd;
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

    private boolean execute(String cmd, StringBuilder stdOut) {
        log.info("Will executing cmd: " + String.join(" ", cmd));
        SystemCmd systemCmd = new SystemCmd();
        systemCmd.setEnv(this.env);
        systemCmd.setWorkDir(workPath);
        return systemCmd.execute(cmd, stdOut);
    }

    public boolean tfInit() {
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute("terraform init", out);
        log.info(out.toString());
        return exeRet;
    }

    public boolean tfPlan() {
        // TODO: Dynamic variables need to be supported.
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute("terraform plan", out);
        log.info(out.toString());
        return exeRet;
    }

    public boolean tfApply() {
        // TODO: Dynamic variables need to be supported.
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute("terraform apply -auto-approve", out);
        log.info(out.toString());
        return exeRet;
    }

    public boolean tfDestroy() {
        // TODO: Dynamic variables need to be supported.
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute("terraform destroy -auto-approve", out);
        log.info(out.toString());
        return exeRet;
    }
}
