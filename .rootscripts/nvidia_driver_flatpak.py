import subprocess
import re

lines = subprocess.check_output(['apt', 'list', "nvidia-driver"]).decode('utf-8').splitlines()
for line in lines:
    if "nvidia-driver" in line:
        version = re.findall("\d+\.\d+\.\d+", line)[0].replace(".", "-")
        subprocess.run(["flatpak", "install", "-y", f"org.freedesktop.Platform.GL32.nvidia-{version}"])
        subprocess.run(["flatpak", "install", "-y", f"org.freedesktop.Platform.GL.nvidia-{version}"])
      
