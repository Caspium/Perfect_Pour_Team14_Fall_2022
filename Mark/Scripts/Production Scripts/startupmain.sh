#!/bin/sh
sudo hciconfig hci0 piscan
sudo hciconfig hci0 sspmode 1
python3 main.py
