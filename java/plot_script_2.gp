set title "20% Insert, 10% Delete, 70% Find\n KeyRange: 10^6, RQSize: 10^{3}" font "Times-Roman,25" offset 0,-1
set size square
set xlabel "Number of Threads" font "Times-Roman,25" offset 0,-3
set ylabel "Throughput (ops/μs)" font "Times-Roman,25" offset -7,0
set xrange [1:128]
set yrange [0:35]
set lmargin 17
set rmargin 16
set bmargin 9
set tmargin 7
set key box at graph 0.77,0.95 font "Times-Roman,22" width 0.5 height 0.5
set xtics (1,2,4,8,16,32,72,128) font "Times-Roman,25" offset -0.9,-1
set ytics font "Times-Roman,25"
#set y2tics () font "Times-Roman,19"
set logscale x 2
set linestyle 1 lt 1 pt 70
set arrow from 72, graph 0 to 72, graph 1 nohead

#plot\
#"toplot.txt" u 1:2 t 'LFCA (no RQers)' w lp lw 3 lt 7 ps 3 pt 5,\
#"toplot.txt" u 1:3 t 'LFCA (+1 RQers)' w lp lw 3 lt 4 ps 3 pt 2,\
#"toplot.txt" u 1:4 t 'LFCA (+4 RQers)' w lp lw 3 lt 2 ps 3 pt 3,\
#"toplot.txt" u 1:5 t 'LFCA (+8 RQers)' w lp lw 3 lt 3 ps 3 pt 13

plot\
"toplot.txt" u 1:2 t 'BPNB-BST 64 (no RQers)' w lp lw 3 lt 7 ps 3 pt 5,\
"toplot.txt" u 1:3 t 'BPNB-BST 64 (+1 RQers)' w lp lw 3 lt 4 ps 3 pt 2,\
"toplot.txt" u 1:4 t 'BPNB-BST 64 (+4 RQers)' w lp lw 3 lt 2 ps 3 pt 3,\
"toplot.txt" u 1:5 t 'BPNB-BST 64 (+8 RQers)' w lp lw 3 lt 3 ps 3 pt 13
