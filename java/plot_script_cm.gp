#set title "40% Insert, 30% Delete, 30% Find, KeyRange: 10^4" font "Times-Roman,25"
#set title "40% Insert, 30% Delete, 15% Find, 15% RQ, KeyRange: 10^6, RQSize: 2^{10}" font "Times-Roman,19"
#set title "40 Updaters (50% Insert, 50% Delete) and 40 RQers, KeyRange: 10^6" font "Times-Roman,20"
#set title "0% Insert, 0% Delete, 100% Find, KeyRange: 10^6" font "Times-Roman,25"
#set title "40% Insert, 30% Delete, 15% Find, 15% RQ\n KeyRange: 10^6, RQSize: 10^{3}" font "Times-Roman,25" offset 0,-1
set title "100% RQ, KeyRange: 10^6, RQSize: 10^{3}" font "Times-Roman,25" offset 0,-1
set size square
set xlabel "Number of Threads" font "Times-Roman,25" offset 0,-3
set ylabel "Cache-misses / Operation" font "Times-Roman,25" offset -9,0
#set xrange [1:128]
#set xrange [8:524288]
set xrange [1:64]
set yrange [0:1600]
set lmargin 15
set rmargin 16
set bmargin 7
set tmargin 6
set key box at graph 0.9,0.93 font "Times-Roman,21" width 0.5 height 0.5
#set xtics (1,2,4,8,16,32,80,128) font "Times-Roman,25" offset -0.9,-1
#set xtics (8,32,128,512,"2K" 2048,"8K" 8192,"32K" 32768,"131K" 131072,"524K" 524288) font "Times-Roman,25" offset -0.9,-1
set xtics (1,2,4,8,16,32,64) font "Times-Roman,25" offset -0.9,-1
set ytics font "Times-Roman,25"
set y2tics (52, 1125) font "Times-Roman,25"
set logscale x 2
set linestyle 1 lt 1 pt 70
#set arrow from 80, graph 0 to 80, graph 1 nohead

plot\
"toplot.txt" u 1:2 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 3 pt 5,\
"toplot.txt" u 1:3 t 'BPNB-BST 64' w lp lw 3 lt rgb "#0020C2" ps 3 pt 11

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
#"toplot.txt" u 1:4 t 'PNB-BST (no validations)' w lp lw 3 lt 6 ps 3 pt 2,\
#"toplot.txt" u 1:4 t 'LFCA' w lp lw 3 lt rgb "#000000" ps 3 pt 1,\

#plot\
#"toplot.txt" u 1:2 t 'NB-BST' w lp lw 3 lt rgb "#008000" ps 3 pt 7,\
#"toplot.txt" u 1:3 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 3 pt 5,\
#"toplot.txt" u 1:4 t 'PNB-BST (optimized)' w lp lw 3 lt 6 ps 3 pt 2

#plot\
#"toplot.txt" u 1:2 t 'NB-BST' w lp lw 3 lt rgb "#008000" ps 3 pt 7,\
#"toplot.txt" u 1:3 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 3 pt 5,\
#"toplot.txt" u 1:4 t 'PNB-BST (no frozen)' w lp lw 3 lt 1 ps 3 pt 13,\
#"toplot.txt" u 1:5 t 'PNB-BST (no validations)' w lp lw 3 lt 6 ps 3 pt 2,\
#"toplot.txt" u 1:6 t 'PNB-BST (same find with BST)' w lp lw 3 lt 3 ps 3 pt 11,\
#"toplot.txt" u 1:7 t 'PNB-BST (same nodes with BST)' w lp lw 3 lt 4 ps 3 pt 9,\
#"toplot.txt" u 1:8 t 'PNB-BST (optimized Insert for prefilling)' w lp lw 3 lt 8 ps 3 pt 1

#plot\
#"toplot.txt" u 1:7 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 3 pt 5,\
#"toplot.txt" u 1:5 t 'KIWI' w lp lw 3 lt rgb "#008000" ps 3 pt 7,\
#"toplot.txt" u 1:6 t 'LFCA 64' w lp lw 3 lt rgb "#000000" ps 3 pt 1,\
#"toplot.txt" u 1:2 t 'PNB-BST 16' w lp lw 3 lt rgb "#00d9ff" ps 3 pt 2,\
#"toplot.txt" u 1:3 t 'PNB-BST 32' w lp lw 3 lt rgb "#1589FF" ps 3 pt 11,\
#"toplot.txt" u 1:4 t 'PNB-BST 64' w lp lw 3 lt rgb "#0020C2" ps 3 pt 9
