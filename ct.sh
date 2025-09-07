#!/bin/bash
screen -d -m -S ct bash -c 'java -jar CryptoTradeBot.jar settings.txt >> out.txt 2>> error.txt'
