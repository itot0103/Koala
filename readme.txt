Koala (Key-node Out Arrangement and LAyout)
by Takayuki Itoh (Ochanomizu University, Japan, E-mail:itot(at)is.ocha.ac.jp)

Implementation of graph visualization technique published as follows:
T. Itoh, K. Klein, Key-node-Separated Graph Clustering and Layout for Human Relationship Graph Visualization, IEEE Computer Graphics and Applications, Vol. 35, No. 6, pp. 30-40, 2015.

Running on Java 1.8 or newer and JOGL (Java binging OpenGL) 2.3.
(Remark that unrecommended packages (java.awt, java.swing, etc.) are used in the code.)

How to run:
1) Invoke "java ocha.itolab.koala.applet.koalaview.KoalaViewer".
2) Press the button "File Open" and select a data file.


Data file format (Please prepare as CSV files):

1) Add the following line before describing nodes
   #connectivity

2) Description of a node:
   (nodeId),(node name)
  (list of connecting nodes)
   (list of connected nodes)
* nodeId must be non-negative integer values.
* node name may not include ",".
* bidirectional connection is allowed.
* make a blank line if the node as no connecting/connected nodes.

3) Add the following line before describing feature values
   #vector,(name1),(name2),....

4) Description of a feature vector of a node
    (nodeId),(value1),(value2),....
* number of values must be same as number of names described in #vector line.
