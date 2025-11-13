package com.xhh.aiagent.rag.documentreader;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class GitHubResource implements Resource{

    private final InputStream inputStream;

    private final GHContent content;

    public GitHubResource(GitHub gitHub, String owner, String repo, String branch, String path) {
        if (Objects.isNull(branch)) {
            branch = "main";
        }
        try {
            content = gitHub.getRepository(owner + "/" + repo).getFileContent(path, branch);
            Assert.isTrue(content.isFile(), "路径下必须是一个文件");
            inputStream = content.read();
        }
        catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public GitHubResource(GHContent content) {
        try {
            this.content = content;
            inputStream = content.read();
        }
        catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public static GitHubResource getInstance(GHContent content) {
        return new GitHubResource(content);
    }

    public GHContent getText() {
        return content;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public URL getURL() throws IOException {
        return null;
    }

    @Override
    public URI getURI() throws IOException {
        return null;
    }

    @Override
    public File getFile() throws IOException {
        return null;
    }

    @Override
    public long contentLength() throws IOException {
        return 0;
    }

    @Override
    public long lastModified() throws IOException {
        return 0;
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        return null;
    }

    @Override
    public String getFilename() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String apiUrl;

        private String gitHubToken;

        private String gitHubTokenOrganization;

        private GitHub gitHub;

        private String owner;

        private String repo;

        private String branch;

        private String path;

        public Builder apiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
            return this;
        }

        public Builder gitHubToken(String gitHubToken) {
            this.gitHubToken = gitHubToken;
            return this;
        }

        public Builder gitHubTokenOrganization(String gitHubTokenOrganization) {
            this.gitHubTokenOrganization = gitHubTokenOrganization;
            return this;
        }

        public Builder gitHub(GitHub gitHub) {
            this.gitHub = gitHub;
            return this;
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder repo(String repo) {
            this.repo = repo;
            return this;
        }

        public Builder branch(String branch) {
            this.branch = branch;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public GitHubResource build() {
            createGithub();
            return new GitHubResource(gitHub, owner, repo, branch, path);
        }

        public List<GitHubResource> buildBatch() {
            createGithub();
            return loadGitHubResources();
        }

        private void createGithub() {
            Assert.notNull(owner, "拥有者必须不为空");
            Assert.notNull(repo, "仓库必须不为空");
            Assert.notNull(path, "路径必须不为空");
            if (Objects.isNull(gitHub)) {
                Assert.notNull(gitHubToken, "GitHub token 必须不为空");
                GitHubBuilder gitHubBuilder = new GitHubBuilder();
                if (apiUrl != null) {
                    gitHubBuilder.withEndpoint(apiUrl);
                }
                if (gitHubToken != null) {
                    if (gitHubTokenOrganization == null) {
                        gitHubBuilder.withOAuthToken(gitHubToken);
                    }
                    else {
                        gitHubBuilder.withOAuthToken(gitHubToken, gitHubTokenOrganization);
                    }
                }
                try {
                    this.gitHub = gitHubBuilder.build();
                }
                catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                }
            }
        }

        private List<GitHubResource> loadGitHubResources() {
            List<GitHubResource> gitHubResources = new ArrayList<>();
            try {
                gitHub.getRepository(owner + "/" + repo)
                        .getDirectoryContent(path, branch)
                        .forEach(ghDirectoryContent -> Builder.scanDirectory(ghDirectoryContent, gitHubResources));
            }
            catch (IOException ioException) {
                throw new RuntimeException(ioException);
            }
            return gitHubResources;
        }

        private static void scanDirectory(GHContent ghContent, List<GitHubResource> gitHubResources) {
            if (ghContent.isDirectory()) {
                try {
                    ghContent.listDirectoryContent()
                            .forEach(ghDirectoryContent -> Builder.scanDirectory(ghDirectoryContent, gitHubResources));
                }
                catch (IOException ioException) {
                    log.error("从 github 读取文件夹失败: {}", ghContent.getHtmlUrl(), ioException);
                }
            }
            else {
                GitHubResource gitHubResource = null;
                try {
                    gitHubResource = GitHubResource.getInstance(ghContent);
                }
                catch (RuntimeException runtimeException) {
                    log.error("从 github 读取文件失败: {}", ghContent.getHtmlUrl(), runtimeException);
                }
                if (gitHubResource != null) {
                    gitHubResources.add(gitHubResource);
                }
            }
        }

    }

}
