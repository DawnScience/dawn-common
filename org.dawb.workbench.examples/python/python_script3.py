



def createRandom():

    import numpy as np
    s = np.random.rand(2048);
    s = s*100
    s = np.ndarray((2048,), buffer=s, dtype=int)
    return s


stack1 = createRandom()
stack2 = createRandom()
stack3 = createRandom()

