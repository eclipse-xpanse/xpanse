/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.utils;

import static org.eclipse.xpanse.modules.deployment.utils.DeploymentScriptsHelper.TF_SCRIPT_FILE_EXTENSION;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.xpanse.modules.deployment.exceptions.DeploymentScriptsCreationFailedException;
import org.eclipse.xpanse.modules.models.common.exceptions.GitRepoCloneException;
import org.eclipse.xpanse.modules.models.servicetemplate.ScriptsRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

/** Bean to manage GIT clone. */
@Slf4j
@Component
public class ScriptsGitRepoManage {

    @Value("${git.command.timeout.seconds:10}")
    private int gitCommandTimeoutSeconds;

    /**
     * Method to check out scripts from a GIT repo.
     *
     * @param workspace directory where the GIT clone must be executed.
     * @param scriptsRepo directory inside the GIT repo where scripts are expected to be present.
     */
    @Retryable(
            retryFor = GitRepoCloneException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public List<File> checkoutScripts(String workspace, ScriptsRepo scriptsRepo) {
        File workspaceDirectory = new File(workspace);
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        repositoryBuilder.findGitDir(workspaceDirectory);
        if (Objects.isNull(repositoryBuilder.getGitDir())) {
            int timeoutSeconds = gitCommandTimeoutSeconds > 0 ? gitCommandTimeoutSeconds : 10;
            CloneCommand cloneCommand = new CloneCommand();
            cloneCommand.setURI(scriptsRepo.getRepoUrl());
            cloneCommand.setProgressMonitor(null);
            cloneCommand.setDirectory(workspaceDirectory);
            cloneCommand.setBranch(scriptsRepo.getBranch());
            cloneCommand.setTimeout(timeoutSeconds);
            try (Git git = cloneCommand.call()) {
                git.checkout();
            } catch (GitAPIException e) {
                String errorMsg =
                        String.format(
                                "Clone scripts from branch %s of repo %s error. %s",
                                scriptsRepo.getBranch(), scriptsRepo.getRepoUrl(), e.getMessage());
                int retryCount =
                        Objects.isNull(RetrySynchronizationManager.getContext())
                                ? 0
                                : RetrySynchronizationManager.getContext().getRetryCount();
                log.error(errorMsg + " Retry count:" + retryCount);
                throw new GitRepoCloneException(errorMsg);
            }
        } else {
            log.info("Scripts repo is already cloned in the workspace.");
        }
        List<File> files = getSourceFiles(workspace, scriptsRepo);
        validateIfFolderContainsTerraformScripts(files, scriptsRepo);
        return files;
    }

    private List<File> getSourceFiles(String workspace, ScriptsRepo scriptsRepo) {
        List<File> sourceFiles = new ArrayList<>();
        File directory =
                new File(
                        workspace
                                + (StringUtils.isNotBlank(scriptsRepo.getScriptsPath())
                                        ? File.separator + scriptsRepo.getScriptsPath()
                                        : ""));
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (Objects.nonNull(files)) {
                Arrays.stream(files)
                        .forEach(
                                file -> {
                                    if (file.isFile()) {
                                        sourceFiles.add(file);
                                    }
                                });
            }
        }
        return sourceFiles;
    }

    private void validateIfFolderContainsTerraformScripts(
            List<File> files, ScriptsRepo scriptsRepo) {
        boolean isScriptsExisted =
                files.stream().anyMatch(file -> file.getName().endsWith(TF_SCRIPT_FILE_EXTENSION));
        if (!isScriptsExisted) {
            throw new DeploymentScriptsCreationFailedException(
                    "No deployment scripts found in the "
                            + scriptsRepo.getRepoUrl()
                            + " repo's '"
                            + (StringUtils.isNotBlank(scriptsRepo.getScriptsPath())
                                    ? File.separator + scriptsRepo.getScriptsPath()
                                    : "root")
                            + "' folder.");
        }
    }
}
