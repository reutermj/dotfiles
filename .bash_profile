rm $HOME/.xinitrc

if [[ -z $DISPLAY ]] && [[ $(tty) = /dev/tty1 ]]; then
export PATH="$HOME/.rootscripts/:$PATH:$HOME/.steamrunscripts/:$HOME/.local/bin/"
cp $HOME/.xinitrc_desktop $HOME/.xinitrc
startx
fi

if [[ -z $DISPLAY ]] && [[ $(tty) = /dev/tty2 ]]; then
export PATH="$HOME/.rootscripts/:$PATH:$HOME/.steamrunscripts/:$HOME/.local/bin/"
cp $HOME/.xinitrc_tv $HOME/.xinitrc
startx
fi

export PATH="$HOME/.cargo/bin:$PATH"
