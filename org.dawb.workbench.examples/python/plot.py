# An exmple jython script able to interact with the graph.
# You will need to show the view 'Plot 1' to see the results of this script.

import scisoftpy as dnp
from time import sleep

def plotLine():
    dnp.plot.setdefname('Plot 1')
    x = dnp.arange(10)
    y = dnp.arange(10)
    dnp.plot.line(x,y)

def plotRandom():
    a  = dnp.random.rand(256,256)
    dnp.plot.image(a)

plotLine()
sleep(4)
plotRandom()
print("Plotting completed")