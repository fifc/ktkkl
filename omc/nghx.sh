#!/usr/bin/zsh

/home/pkg/bin/nghttpx -f'*,80;no-tls' -b"127.0.0.1,10080;;proto=h2" > log/nghttpx.out 2>&1 &
