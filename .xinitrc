setxkbmap -option caps:super
#xrandr --output HDMI-0 --mode 1280x1024 --rate 60
xrandr --output DP-2 --mode 1920x1080 --rate 144
#xrandr --output HDMI-0 --mode 1280x720 --rate 60
feh --bg-scale ~/.wallpapers/walpaper.png
xrdb -merge ~/.Xresources
compton &
exec i3
