#set title "100% RQ, KeyRange: 10^6, RQSize: 10^{3}" font "Times-Roman,25" offset 0,-1
set title "100% Find, KeyRange: 10^6" font "Times-Roman,25"
#set title "0% Insert, 0% Delete, 100% Find, KeyRange: 10^6" font "Times-Roman,25"
set size square
set xlabel "Number of Threads" font "Times-Roman,25" offset 0,-3
set ylabel "Throughput (ops/μs)" font "Times-Roman,25" offset -7,0
set xrange [1:128]
set yrange [0:70]
set lmargin 17
set rmargin 16
set bmargin 7
set tmargin 6
set key box at graph 0.6,0.92 font "Times-Roman,25" width 0.5 height 0.5
set xtics (1,2,4,8,16,32,80,128) font "Times-Roman,25" offset -0.9,-1
set ytics font "Times-Roman,25"
#set y2tics (22, 25, 26, 29, 30) font "Times-Roman,20"
set logscale x 2
set linestyle 1 lt 1 pt 70
set arrow from 80, graph 0 to 80, graph 1 nohead
#set arrow from 32, graph 0 to 32, graph 1 nohead

plot\
"toplot.txt" u 1:2 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 3 pt 5,\
"toplot.txt" u 1:3 t 'BPNB-BST 64' w lp lw 3 lt rgb "#0020C2" ps 3 pt 11

#plot\
#"toplot.txt" u 1:2 t 'NB-BST' w lp lw 3 lt rgb "#008000" ps 3 pt 7,\
#"toplot.txt" u 1:3 t 'KIWI' w lp lw 3 lt rgb "#8B02CF" ps 3 pt 3,\
#"toplot.txt" u 1:4 t 'LFCA' w lp lw 3 lt rgb "#000000" ps 3 pt 2,\
#"toplot.txt" u 1:5 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 3 pt 5,\
#"toplot.txt" u 1:6 t 'BPNB-BST 16' w lp lw 3 lt rgb "#00d9ff" ps 3 pt 9,\
#"toplot.txt" u 1:7 t 'BPNB-BST 64' w lp lw 3 lt rgb "#0020C2" ps 3 pt 11

#plot\
#"toplot.txt" u 1:2 t 'NB-BST' w lp lw 3 lt rgb "#008000" ps 3 pt 7,\
#"toplot.txt" u 1:3 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 3 pt 5,\
#"toplot.txt" u 1:4 t 'BPNB-BST 64' w lp lw 3 lt rgb "#0020C2" ps 3 pt 11,\
#"toplot.txt" u 1:5 t 'LFCA' w lp lw 3 lt rgb "#000000" ps 3 pt 2


#plot\
#"toplot.txt" u 1:2 t 'NB-BST' w lp lw 3 lt rgb "#008000" ps 3 pt 7,\
#"toplot.txt" u 1:3 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 3 pt 5,\
#"toplot.txt" u 1:4 t 'LFCA 64' w lp lw 3 lt rgb "#000000" ps 3 pt 2,\
#"toplot.txt" u 1:5 t 'LFCA 10' w lp lw 3 lt 3 ps 3 pt 11,\
#"toplot.txt" u 1:6 t 'LFCA 2' w lp lw 3 lt 4 ps 3 pt 9

#plot\
#"toplot.txt" u 1:2 t 'NB-BST' w lp lw 3 lt rgb "#008000" ps 3 pt 7,\
#"toplot.txt" u 1:3 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 3 pt 5,\
#"toplot.txt" u 1:4 t 'PNB-BST (optimized)' w lp lw 3 lt 6 ps 3 pt 2

#plot\
#"toplot.txt" u 1:2 t 'NB-BST' w lp lw 3 lt rgb "#008000" ps 3 pt 7,\
#"toplot.txt" u 1:3 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 3 pt 5,\
#"toplot.txt" u 1:4 t 'PNB-BST (no validations)' w lp lw 3 lt 6 ps 3 pt 2,\
#"toplot.txt" u 1:4 t 'LFCA' w lp lw 3 lt rgb "#000000" ps 3 pt 1,\

#plot\
#"toplot.txt" u 1:2 t 'NB-BST' w lp lw 3 lt rgb "#008000" ps 3 pt 7,\
#"toplot.txt" u 1:3 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 3 pt 5,\
#"toplot.txt" u 1:4 t 'PNB-BST (no frozen)' w lp lw 3 lt 1 ps 3 pt 13,\
#"toplot.txt" u 1:5 t 'PNB-BST (no validations)' w lp lw 3 lt 6 ps 3 pt 2,\
#"toplot.txt" u 1:6 t 'PNB-BST (same find with BST)' w lp lw 3 lt 3 ps 3 pt 11,\
#"toplot.txt" u 1:7 t 'PNB-BST (same nodes with BST)' w lp lw 3 lt 4 ps 3 pt 9,\
#"toplot.txt" u 1:8 t 'PNB-BST (optimized Insert for prefilling)' w lp lw 3 lt 8 ps 3 pt 1

#plot\
#"toplot.txt" u 1:2 t 'NB-BST' w lp lw 3 lt rgb "#008000" ps 3 pt 7,\
#"toplot.txt" u 1:3 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 3 pt 5,\

#plot\
#"toplot.txt" u 1:7 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 3 pt 5,\
#"toplot.txt" u 1:5 t 'KIWI' w lp lw 3 lt rgb "#008000" ps 3 pt 7,\
#"toplot.txt" u 1:6 t 'LFCA 64' w lp lw 3 lt rgb "#000000" ps 3 pt 1,\
#"toplot.txt" u 1:2 t 'PNB-BST 16' w lp lw 3 lt rgb "#00d9ff" ps 3 pt 2,\
#"toplot.txt" u 1:3 t 'PNB-BST 32' w lp lw 3 lt rgb "#1589FF" ps 3 pt 11,\
#"toplot.txt" u 1:4 t 'PNB-BST 64' w lp lw 3 lt rgb "#0020C2" ps 3 pt 9

#plot\
#"toplot.txt" u 1:5 t 'KIWI' w lp lw 3 lt rgb "#D37F36" ps 3 pt 7,\
#"toplot.txt" u 1:2 t 'NB-BST' w lp lw 3 lt 2 ps 3 pt 7,\
#"toplot.txt" u 1:4 t 'LFCA 64' w lp lw 3 lt 8 ps 3 pt 3,\
#"toplot.txt" u 1:5 t 'LFCA 16' w lp lw 3 lt 1 ps 3 pt 1,\
#"toplot.txt" u 1:6 t 'LFCA 2' w lp lw 3 lt 6 ps 3.5 pt 9,\
#"toplot.txt" u 1:3 t 'PNB-BST' w lp lw 3 lt 7 ps 3 pt
