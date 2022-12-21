package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.packer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.utils.SystemCmd;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions.PackerExecutorException;
import org.eclipse.osc.services.ocl.loader.Artifact;
import org.eclipse.osc.services.ocl.loader.Ocl;

@Slf4j
public class PackerExecutor {

    private static final String IMAGE = ": An image was created:";

    private final Ocl ocl;
    private final Artifact artifact;
    private final Map<String, String> env;
    private String packerPath;
    private String workPath;

    public PackerExecutor(final Ocl ocl, final Artifact artifact, Map<String, String> env) {
        this.ocl = ocl;
        this.artifact = artifact;
        this.env = env;
    }

    public void createWorkspace() {
        File ws = new File("packer_ws" + FileSystems.getDefault().getSeparator() + ocl.getName()
            + FileSystems.getDefault().getSeparator() + artifact.getName());

        if (!ws.exists() && !ws.mkdirs()) {
            throw new PackerExecutorException(
                "Create workspace for PackerExecutor failed, File path not created: "
                    + ws.getAbsolutePath());
        }

        workPath = ws.getAbsolutePath();
        log.info("Packer working directory is " + workPath);
    }

    public void createInstallScript() {
        Ocl2Packer ocl2Packer = new Ocl2Packer(ocl, artifact);
        String installScript = ocl2Packer.getInstallScript();
        String installScriptPath =
            workPath + FileSystems.getDefault().getSeparator() + "install_script.sh";

        try {
            try (FileWriter scriptFile = new FileWriter(installScriptPath)) {
                scriptFile.write(installScript);
            }
        } catch (IOException ex) {
            throw new PackerExecutorException(
                "PackerExecutor create packer install script failed.", ex);
        }
    }

    public void createPackerScript(PackerVars packerVars) {
        Ocl2Packer ocl2Packer = new Ocl2Packer(ocl, artifact);
        String packerScript = ocl2Packer.getHclImages(packerVars);
        packerPath = workPath + FileSystems.getDefault().getSeparator() + "huaweicloud.pkr.hcl";

        try {
            try (FileWriter scriptFile = new FileWriter(packerPath)) {
                // Packer plugin version
                scriptFile.write(""
                    + "packer {\n"
                    + "  required_plugins {\n"
                    + "    huaweicloud-ecs = {\n"
                    + "      version = \">= 0.4.0\"\n"
                    + "      source = \"github.com/huaweicloud/huaweicloud\"\n"
                    + "    }\n"
                    + "  }\n"
                    + "}");
                // Packer configuration
                scriptFile.write(packerScript);
            }
        } catch (IOException ex) {
            throw new PackerExecutorException("PackerExecutor create packer script failed.", ex);
        }
    }

    private boolean execute(String cmd, StringBuilder stdOut) {
        log.info("Will executing cmd: " + String.join(" ", cmd));
        SystemCmd systemCmd = new SystemCmd();
        systemCmd.setEnv(this.env);
        systemCmd.setWorkDir(workPath);
        return systemCmd.execute(cmd, stdOut);
    }

    public boolean packerInit() {
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute("packer init huaweicloud.pkr.hcl", out);
        log.info(out.toString());
        return exeRet;
    }

    public String packerBuild() {
        String imageId = "";
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute("packer build huaweicloud.pkr.hcl", out);
        if (!exeRet) {
            throw new PackerExecutorException("PackerExecutor.packerBuild failed.");
        }
        log.info(out.toString());

        String[] lines = out.toString().split("\\n");
        for (String line : lines) {
            if (line.indexOf(IMAGE) != -1) {
                String[] s = line.split(" ");
                imageId = s[s.length - 1];
                break;
            }
        }

        return imageId;
    }
}
