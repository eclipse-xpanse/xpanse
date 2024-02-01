/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.utils;

import java.io.File;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.xpanse.modules.models.common.exceptions.GitRepoCloneException;
import org.eclipse.xpanse.modules.models.servicetemplate.ScriptsRepo;
import org.springframework.stereotype.Component;

/**
 * Bean to manage GIT clone.
 */
@Slf4j
@Component
public class ScriptsGitRepoManage {

    /**
     * Method to check out scripts from a GIT repo.
     *
     * @param workspace directory where the GIT clone must be executed.
     * @param scriptsRepo directory inside the GIT repo where scripts are expected to be present.
     */
    public void checkoutScripts(String workspace,
                                ScriptsRepo scriptsRepo) {
        log.info("Cloning GIT repo to get the deployment scripts");
        File workspaceDirectory = new File(workspace);
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        repositoryBuilder.findGitDir(workspaceDirectory);
        if (Objects.isNull(repositoryBuilder.getGitDir())) {
            log.info("Repo does not exist in the workspace. Cloning it.");
            try {
                CloneCommand cloneCommand = new CloneCommand();
                cloneCommand.setURI(scriptsRepo.getRepoUrl());
                cloneCommand.setProgressMonitor(null);
                cloneCommand.setDirectory(workspaceDirectory);
                cloneCommand.setBranch(scriptsRepo.getBranch());
                cloneCommand.call();
            } catch (GitAPIException e) {
                log.error(e.getMessage(), e);
                throw new GitRepoCloneException(e.getMessage());
            }
        } else {
            log.info("Scripts repo is already cloned in the workspace.");
        }
        folderContainsScripts(workspace, scriptsRepo);
    }

    private void folderContainsScripts(String workspace, ScriptsRepo scriptsRepo) {
        File directory = new File(workspace
                + (Objects.nonNull(scriptsRepo.getScriptsPath())
                        ? File.separator + scriptsRepo.getScriptsPath()
                        : ""));
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".tf"));
        if (Objects.isNull(files) || files.length == 0) {
            throw new GitRepoCloneException(
                    "No terraform scripts found in the "
                            + scriptsRepo.getRepoUrl()
                            + " repo's '"
                            + (Objects.nonNull(scriptsRepo.getScriptsPath())
                            ? scriptsRepo.getScriptsPath() : "root")
                            + "' folder.");
        }
    }
}
