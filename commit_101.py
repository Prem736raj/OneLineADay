import subprocess

def get_output(cmd):
    result = subprocess.run(cmd, stdout=subprocess.PIPE, text=True, shell=True)
    return result.stdout.strip()

def run_cmd(cmd_list):
    subprocess.run(cmd_list, check=True)

# Stage all files
print("Staging all files...")
run_cmd(["git", "add", "-A"])

# Get all staged files
staged_files_output = get_output("git diff --cached --name-only")
staged_files = [f for f in staged_files_output.splitlines() if f.strip()]

# Unstage all files
print("Unstaging to process one by one...")
run_cmd(["git", "reset"])

target_commits = 101
commits_made = 0

if len(staged_files) >= target_commits:
    # We have enough files, commit them one by one until the last commit
    for i in range(target_commits - 1):
        file = staged_files[i]
        run_cmd(["git", "add", file])
        run_cmd(["git", "commit", "-m", f"Update {file.split('/')[-1]}"])
        commits_made += 1
        print(f"Commit {commits_made}/{target_commits}: {file}")
    
    # Commit remaining files in the 101st commit
    for file in staged_files[target_commits - 1:]:
        run_cmd(["git", "add", file])
    run_cmd(["git", "commit", "-m", "Final updates"])
    commits_made += 1
    print(f"Commit {commits_made}/{target_commits}: Remaining {len(staged_files) - target_commits + 1} files")
else:
    # We have fewer files than 101, commit each file, then add empty commits
    for file in staged_files:
        run_cmd(["git", "add", file])
        run_cmd(["git", "commit", "-m", f"Update {file.split('/')[-1]}"])
        commits_made += 1
        print(f"Commit {commits_made}/{target_commits}: {file}")
    
    # Add empty commits until we reach exactly 101
    while commits_made < target_commits:
        run_cmd(["git", "commit", "--allow-empty", "-m", "Minor adjustments"])
        commits_made += 1
        print(f"Commit {commits_made}/{target_commits}: Empty commit")

print("Pushing to GitHub...")
run_cmd(["git", "push"])
print("Successfully pushed 101 commits!")
