

def createRandom():

    import numpy as np
    s = np.random.rand(2048, 2048);
    s = s*100
    s = np.ndarray((2048,2048), buffer=s, dtype=int)
    return s


image1 = createRandom()
image2 = createRandom()
image3 = createRandom()
