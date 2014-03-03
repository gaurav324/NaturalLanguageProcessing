import os
import subprocess

# Helper script.
files = subprocess.check_output("ls *random_?_tp_0.8.err", shell=True).split("\n")
for f in files:
    tail = subprocess.check_output("tail -6 " + f, shell=True)
    ans = [f]
    for line in tail.split("\n"):
        if "Training accuracy=" in line:
            ans.append(line[len("Training accuracy="):])
        if "Testing accuracy=" in line:
            ans.append(line[len("Training accuracy="):])
        if "Testing OOVAccuracy=" in line:
            ans.append(line[len("Training OOVAccuracy="):])
    print "=".join(ans)
