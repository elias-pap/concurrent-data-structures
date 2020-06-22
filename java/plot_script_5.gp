set title "40 Updaters (50% Insert, 50% Delete)\n+ 40 RQers, KeyRange: 10^6 - read Counter before handshaking" font "Times-Roman,25" offset 0,-1
set size square
set xlabel "RangeQuery Size" font "Times-Roman,25" offset 0,-2.5
set ylabel "Throughput of Updaters (ops/μs)" font "Times-Roman,25" offset -6,0
set xrange [8:524288]
set yrange [0:12]
set lmargin 17
set rmargin 16
set bmargin 7
set tmargin 7.1
set key box at graph 0.97,0.29 font "Times-Roman,18" width 0.5 height 0.5
set xtics (8,32,128,512,"2K" 2048,"8K" 8192,"32K" 32768,"128K" 131072,"512K" 524288) font "Times-Roman,25" offset -0.9,-1
set ytics font "Times-Roman,25"
#set y2tics (0.5, 0.7, 7, 7.6, 1) font "Times-Roman,25"
set logscale x 2
set linestyle 1 lt 1 pt 70

plot\
"toplot.txt" u 1:2 t 'KIWI' w lp lw 3 lt rgb "#8B02CF" ps 3.5 pt 3,\
"toplot.txt" u 1:3 t 'LFCA' w lp lw 3 lt rgb "#000000" ps 3.5 pt 2,\
"toplot.txt" u 1:4 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 3.5 pt 5,\
"toplot.txt" u 1:5 t 'BPNB-BST 16' w lp lw 3 lt rgb "#00d9ff" ps 3.5 pt 9,\
"toplot.txt" u 1:6 t 'BPNB-BST 32' w lp lw 3 lt rgb "#1589FF" ps 3.5 pt 13,\
"toplot.txt" u 1:7 t 'BPNB-BST 64' w lp lw 3 lt rgb "#0020C2" ps 3.5 pt 11

#plot\
#"toplot.txt" u 1:7 t 'PNB-BST' w lp lw 3 lt rgb "#E41B17" ps 3 pt 5,\
#"toplot.txt" u 1:5 t 'KIWI' w lp lw 3 lt rgb "#8B02CF" ps 3 pt 3,\
#"toplot.txt" u 1:6 t 'LFCA 64' w lp lw 3 lt rgb "#000000" ps 3 pt 2,\
#"toplot.txt" u 1:2 t 'PNB-BST 16' w lp lw 3 lt rgb "#00d9ff" ps 3 pt 9,\
#"toplot.txt" u 1:3 t 'PNB-BST 32' w lp lw 3 lt rgb "#1589FF" ps 3 pt 13,\
#"toplot.txt" u 1:4 t 'PNB-BST 64' w lp lw 3 lt rgb "#0020C2" ps 3 pt 11
