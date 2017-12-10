
alias tf='tail -f'
alias ls='ls --color'
alias ll='ls --color -l'

xpath=/pkg/bin:/opt/bin
if [[ -z "$PATH" ]]; then
	PATH=${xpath}
else
	PATH=${xpath}:$PATH
fi
export PATH

xpath=/pkg/lib:/opt/lib
if [[ -z "$LD_LIBRARY_PATH" ]]; then
	LD_LIBRARY_PATH=${xpath}
else
	LD_LIBRARY_PATH=${xpath}:$LD_LIBRARY_PATH
fi
export LD_LIBRARY_PATH

xpath=/pkg/lib/python3.6/site-packages
if [[ -z "$PYTHONPATH" ]]; then
	PYTHONPATH=${xpath}
else
	PYTHONPATH=${xpath}:$LD_LIBRARY_PATH
fi
export PYTHONPATH

xpath=/pkg/lib/pkgconfig
if [[ -z "$PKG_CONFIG_PATH" ]]; then
	PKG_CONFIG_PATH=${xpath}
else
	PKG_CONFIG_PATH=${xpath}:$PKG_CONFIG_PATH
fi
export PKG_CONFIG_PATH
unset xpath

