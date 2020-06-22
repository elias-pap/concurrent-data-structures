set title "72 Threads (40% Insert, 30% Delete, 30% Find), KeyRange: 10^6" font "Times-Roman,20"
set size square
set xlabel "RangeQuery Size" font "Times-Roman,25" offset 0,-2.5
set ylabel "Throughput (ops/μs)" font "Times-Roman,25" offset -7,0
set xrange [8:524288]
set yrange [0:30]
set lmargin 17
set rmargin 16
set bmargin 7
set tmargin 6
set key box at graph 0.75,0.95 font "Times-Roman,20" width 1.5 height 0.5
set xtics (8,32,128,512,"2K" 2048,"8K" 8192,"32K" 32768,"128K" 131072,"512K" 524288) font "Times-Roman,25" offset -0.9,-1
set ytics font "Times-Roman,25"
#set format y "10^{%L}"
#set ytics (0.0001, 0.001, 0.01, 0.1, "1" 1, "10" 10) font "Times-Roman,25"
#set y2tics (0.5, 0.7, 7, 7.6, 1) font "Times-Roman,25"
set logscale x 2
#set logscale y 10
set linestyle 1 lt 1 pt 70

plot\
"toplot.txt" u 1:2 t 'PNB-BST 64 (no RQers)' w lp lw 3 lt 7 ps 3 pt 5,\
"toplot.txt" u 1:3 t 'PNB-BST 64 (+1 RQers)' w lp lw 3 lt 4 ps 3 pt 2,\
"toplot.txt" u 1:4 t 'PNB-BST 64 (+4 RQers)' w lp lw 3 lt 2 ps 3 pt 3,\
"toplot.txt" u 1:5 t 'PNB-BST 64 (+8 RQers)' w lp lw 3 lt 3 ps 3 pt 13
