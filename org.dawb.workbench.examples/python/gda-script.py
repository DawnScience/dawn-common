

# Comments will be ignored at the Jython level
pos simpleScannable
scan simpleScannable 0 ${scansize} 1
pos simpleScannable


from time import sleep
sleep(1) # No need to wait here actually


pos simpleScannable
scan simpleScannable ${scansize} 0 -1
pos simpleScannable
