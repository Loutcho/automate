# automate
Cellular Automata

=== Cellular automaton #1 "Main" ===

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
```
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
```

means, for instance, that the alive cells die when n = 0, 1, 7, or 8;
that they also die when n = 2 or 6, unless they are surrounded only by neighbours of the same color in which case they change allegiance;
etc.

The aerated triangular shape is the consequence of n = w + b, |d| = |w - b|.

=== Cellular automaton #1 "My Neighbor is Rich" ===

Cellular automaton inspired from "class struggle".
Each cell has its wealth, a real number randomly initialized between 0 and 1.
The evolution of cell (i, j) is governed by the following rules :
- let p be the count of neighbors of (i, j) that are substantially poorer than (i, j), that is to say such that their difference of wealth compared to (i, j) is more than a threshold D;
- if p is greater than or equal to P (another threshold), then REVOLUTION: these p neighbors of (i, j) revolt and steal from (i, j) a proportion R of its wealth. They divide it among themselves proportionnally to the differences of wealth;
- otherwise WORSENING INEQUALITIES: (i, j) keeps essentially its wealth but has to pay a tax (rate I) to each neighbor richer than him/her.
