if [[ -z $DISPLAY ]] && [[ $(tty) = /dev/tty1 ]]; then
export PATH="$HOME/.rootscripts/:$PATH:$HOME/.steamrunscripts/:$HOME/.local/bin/"
mountdrives &
startx
fi

