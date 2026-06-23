# D11 SemVer and Gitflow Release: CI/CD Demo

**Demo 5 in the AXA Secure SDLC Training (slide 51).**

This demo shows the release process: how code moves through a Gitflow branching model, how Semantic Versioning is applied, and how a release is cut, tagged, SBOM-generated, and published to an artifact registry. Unlike the scanning demos, this one is about PROCESS and release discipline, not finding vulnerabilities.

## What this demo proves

A controlled release does not happen by someone manually editing a version and uploading a jar. It is a disciplined, automated sequence:
- Changes flow feature branch to develop via reviewed PRs
- Releases are cut from a stable branch with proper version numbers
- The release version strips -SNAPSHOT, gets tagged, and an SBOM is produced
- The artifact is published to a registry, then the version bumps to the next development iteration

## The branch model (Gitflow)

```
main      o---------------------o (v1.3.4 tagged, production)
           \                   /
develop     o---o---o---o-----o   (integration branch, always -SNAPSHOT)
                 \     /
feature           o---o             (short-lived, one change, PR into develop)
```

- main: production-ready, tagged releases only
- develop: integration branch, always carries a -SNAPSHOT version
- feature/*: short-lived branches for individual changes, merged into develop via reviewed PR
- The release is cut by promoting develop to main, stripping -SNAPSHOT, and tagging

## Semantic Versioning

Version format MAJOR.MINOR.PATCH (for example 1.3.4):
- MAJOR: breaking changes
- MINOR: new backward-compatible features
- PATCH: backward-compatible bug fixes
- -SNAPSHOT suffix marks an in-development version that is not yet released

The release workflow turns 1.3.4-SNAPSHOT into 1.3.4 (the release), tags it v1.3.4, then bumps develop to 1.3.5-SNAPSHOT for the next iteration.

## What's in this folder

```
.
├── README.md                          (you are here)
├── pom.xml                            (version 1.3.4-SNAPSHOT, CycloneDX + versions plugins)
├── src/main/java/com/axa/demo/App.java
├── src/test/java/com/axa/demo/AppTest.java
└── .github/workflows/
    ├── ci.yml                         (build + test on PRs into develop)
    └── release.yml                    (the manual release workflow)
```

## A note on the registry (JFrog vs GitHub Packages)

Slide 51 Activity 4 says "deploy to JFrog Artifactory". This demo deploys to GitHub Packages instead, because it is a real Maven artifact registry, free, and built into GitHub with no extra infrastructure to stand up. The artifact genuinely publishes, so you see a real registry entry during the demo.

The mechanism is identical to Artifactory: the pom distributionManagement points at a repository URL, Maven deploy pushes the artifact and its SBOM there. For AXA, you would swap the GitHub Packages URL for the Artifactory repository URL and use Artifactory credentials. Say this during the demo: "AXA publishes to JFrog Artifactory; here we use GitHub Packages as the free equivalent, the workflow is the same, only the registry URL and credentials change."

## Trainer setup (one-time, before the session)

### Step 1: Edit the pom distributionManagement URL

In pom.xml, change the GitHub Packages URL to your repo:
```xml
<url>https://maven.pkg.github.com/YOUR-ORG/YOUR-REPO</url>
```

### Step 2: Push as main, then create develop

```bash
git init
git add .
git commit -m "Initial project at 1.3.4-SNAPSHOT"
git branch -M main
git remote add origin https://github.com/YOUR-ORG/YOUR-REPO.git
git push -u origin main

git checkout -b develop
git push -u origin develop
```

### Step 3: Make the repo public (free-plan branch protection)

Settings -> General -> Change visibility -> Make public.

### Step 4: Branch protection on develop (the 2-reviewer gate)

Settings -> Branches -> Add rule:
- Branch name pattern: `develop`
- Require a pull request before merging
- Require approvals: set to 2 (the slide says 2 reviewers including a Security Champion)
- Require status checks to pass before merging -> select `Build and Test`
- Save

Note: enforcing 2 approvals in a live demo means you need 2 accounts to actually approve, or you demonstrate the BLOCK (PR cannot merge without the approvals) rather than completing the merge. For a solo recording, showing the blocked state with "2 approvals required" is the realistic visual. If you have a co-presenter account, you can show one approval landing and the second still required.

### Step 5: Branch protection on main

Same idea, protect main so releases are controlled. Require a PR and the Build and Test check.

## During the session (the demo flow)

### Activity 1: Feature branch, commit, open PR (2 min)

```bash
git checkout develop
git checkout -b feature/update-greeting
# make a small change to App.java
git add . && git commit -m "Update greeting text"
git push -u origin feature/update-greeting
```
Open a PR from feature/update-greeting into develop. Show the CI (Build and Test) running on the PR.

### Activity 2: Merge to develop, observe required approvals (1.5 min)

On the PR, scroll to the merge area. Show "Review required" and "2 approvals required". This is the Security Champion gate. Explain: changes cannot reach develop without two reviewers, one of whom is the Security Champion. Show the Merge button disabled until approvals land.

### Activity 3: Run the release workflow (2 min)

Go to Actions -> Release -> Run workflow. Enter:
- release_version: 1.3.4
- next_version: 1.3.5

Watch the workflow:
- Strips -SNAPSHOT (1.3.4-SNAPSHOT becomes 1.3.4)
- Builds, tests, generates the CycloneDX SBOM
- Publishes to the registry
- Tags v1.3.4
- Bumps develop/main to 1.3.5-SNAPSHOT

### Activity 4: SBOM, registry, version bump (1.5 min)

Show the results:
- The SBOM artifact (sbom-1.3.4) on the workflow run, download and open it (CycloneDX JSON/XML listing every dependency)
- The published package under the repo's Packages section (the real registry entry)
- The new tag v1.3.4 under Tags
- The pom now at 1.3.5-SNAPSHOT (next development iteration)

## What this teaches

Releases are disciplined and automated, not ad hoc. Semantic Versioning communicates the nature of each change. Gitflow keeps production (main) stable while integration (develop) moves fast. The release workflow makes versioning, tagging, SBOM generation, and publishing repeatable and auditable. The SBOM (CycloneDX) is the supply-chain record of exactly what shipped, tying back to the Demo 1 supply-chain lesson.

## Verification status (honest disclosure)

The pom and both workflows are structurally validated (XML and YAML parse, version-transition logic confirmed). However, the Maven commands (versions:set, package with CycloneDX, deploy) could NOT be executed in the environment used to build this, because Maven Central was not reachable there. Before your session, run it once on GitHub and confirm:
- The release workflow strips -SNAPSHOT, builds, and produces target/bom.xml and target/bom.json
- The deploy step publishes to GitHub Packages (check the repo Packages section)
- The tag v1.3.4 appears and the pom bumps to 1.3.5-SNAPSHOT

Most likely first-run adjustments: the GitHub Packages URL must match your repo exactly (OWNER/REPO), and the GITHUB_TOKEN needs packages: write permission (already set in the workflow).

## Reference

- AXA SSDLC-ASM-01 (asset and version management), SSDLC-IAM-01 (access/approvals), SSDLC-DEV-02 (artifact management)
- Semantic Versioning: https://semver.org
- Gitflow branching model
- Tools named on slide 51: Git, Maven, CycloneDX, JFrog Artifactory, GitHub branch protection
