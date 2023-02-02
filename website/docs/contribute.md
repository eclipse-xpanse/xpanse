---
sidebar_position: 9
---

# Contribute

Open Services Cloud is an open source project. We love contributions !

There are many ways you can contribute on Open Services Cloud:

* Browse the [documentation](intro): if something is confusing, missing, or not clear, let us know.
* [Clone and build the project, try it out](runtime)
* [Browse the source code](https://github.com/huaweicloud/osc)

## Report bugs and feature requests

You find a bug or want something implemented ? Please report an issue in
our [issue tracker](https://github.com/huaweicloud/osc/issues).

When creating a bug make sure you document the steps to reproduce the issue and provide all necessary information like
OS, versions your use, logs. When creating a feature request document your requirements first. Try to not directly
describe the solution.

If you want to dive into development yourself then you can also browse for open issues or features that need to be
implemented. Take ownership of an issue and try fix it. Before doing a bigger change describe the concept/design of what
you plan to do. If unsure if the design is good or will be accepted discuss it on GitHub.

## Create Pull Request

The best way to provide changes is to fork `osc` repo on github and provide a pull request with your changes. To make it
easy to apply your changes please use the following conventions :

* Every pull request should be "attached" to an issue.
* Create a branch using:

```shell
$ git clone https://github.com/huaweicloud/osc
git fetch --all
git checkout -b my-branch origin/main
```

* Don't forget to periodically rebase your branch:

```shell
$ git pull --rebase
$ git push GitHubUser my-branch --force
```

* If you have a group of commits related to the same change, please squash your commits into one and force push your
  branch using `git rebase -i origin/main`
* Test that your change works by adapting or adding tests.
* Follow the boy scout rule to "Always leave the campground cleaner than you found it."
* Make sure you do a build before doing a PR. The build has to be successfull :

```shell
$ mvn clean verify
```

* If your PR has conflicts with the main then rebase the branch. PRs with conflicts are unlikely to be applied
* Do not change too much in a PR. The smaller the PR the easier it is to apply and the faster it will be done
* Even if we are monitoring closely the PR, if you think your PR doesn't move forward fast enough, do not hesitate to
  ping in a PR comment to get some update.
