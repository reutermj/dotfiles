on first install
set kernel parameter `nomodeset`
then after install: 

```
sudo nano /etc/default/grub
GRUB_CMDLINE_LINUX_DEFAULT="nomodeset"
```

to get bluetooth controller to work:
* update kernel
* https://github.com/atar-axis/xpadneo
* set in /etc/bluetooth/input.conf
```
[General]
UserspaceHID=true
```

auto login
```
sudo systemctl edit getty@tty1

[Service]
ExecStart=
ExecStart=-/sbin/agetty --autologin mark --noclear %I 38400 linux

systemctl enable getty@tty1.service
restart
```

For whatever reason, this fixes steam...
```
sudo apt remove -y --purge xdg-desktop-portal* && sudo apt autoremove -y --purge
```

This seems to work for setting up vortex

https://github.com/pikdum/steam-deck/tree/master
