# automate
Cellular Automaton

Inspired from Life, but with more complex rules. A cell is either dead or alive-white or alive-black
- what matters for a cell:
-- the number n of alive neighbours
-- the difference d between the number of alive neighbours of the same color and the number of alive neighbours of the other color

There are rules to tell
- when a dead cell becomes alive (case "+")
- when an alive cell dies (case "X")
- when an alive cell survives and keeps its color (case "=")
- when an alive cell survives and changes its color (case "S")

The rules are stored in the file rule.txt with an ASCII art format, e.g.
    --------
d-> 87654321012345678
n=8 X X X X X X X X X
n=7  X X X X X X X X 
n=6   X X X X X X S  
n=5    S S = = = X   
n=4     S = = = =    
n=3      S S = =     
n=2       X X S      
n=1        X X       
n=0         X        

means, for instance, that the alive cells die when n = 0, 1, 7, or 8;
that they also die when n = 2 or 6, unless they are surrounded only by neighbours of the same color in which case they change allegiance;
etc.

The aerated triangular shape is the consequence of n = w + b, |d| = |w - b|.
