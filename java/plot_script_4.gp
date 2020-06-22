set title "40% Insert, 30% Delete, 15% Find, 15% RQ\n KeyRange: 10^6, RQSize: 10^{3}" font "Times-Roman,25" offset 0,-1
set size square
set xlabel "Number of Threads" font "Times-Roman,25" offset 0,-3
set ylabel "Throughput (ops/μs)" font "Times-Roman,25" offset -7,0
set xrange [1:128]
set yrange [0:9]
set lmargin 17
set rmargin 16
set bmargin 9
set tmargin 7
set key box at graph 0.63,0.94 font "Times-Roman,27" width 0.5 height 0.5
set xtics (1,2,4,8,16,32,80,128) font "Times-Roman,25" offset -0.9,-1
set ytics font "Times-Roman,25"
#set y2tics (1.6, 5.7) font "Times-Roman,11"
set logscale x 2
set linestyle 1 lt 1 pt 70
set arrow from 80, graph 0 to 80, graph 1 nohead

plot\
"toplot.txt" u 1:2 t 'KIWI' w lp lw 3 lt rgb "#8B02CF" ps 3 pt 3,\
"toplot.txt" u 1:3 t 'LFCA 64' w lp lw 3 lt rgb "#000000" ps 3 pt 2,\
"toplot.txt" u 1:4 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 3 pt 5,\
"toplot.txt" u 1:5 t 'PNB-BST 16' w lp lw 3 lt rgb "#00d9ff" ps 3 pt 9,\
"toplot.txt" u 1:6 t 'PNB-BST 64' w lp lw 3 lt rgb "#0020C2" ps 3 pt 11
