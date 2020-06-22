set title "40 Updaters (50% Insert, 50% Delete) +40 RQers	KeyRange: 10^6" font "Times-Roman,25"
set xlabel "RangeQuery Size" font "Times-Roman,25" offset 0,-3
set ylabel "Keys/treap" font "Times-Roman,25" offset -10,0
#set xrange [88:524288]
#set yrange [0:8000]
set autoscale
set lmargin 22
set rmargin 13
set bmargin 7
set key box at graph 0.32,0.9 font "Times-Roman,27" width 1.5 height 0.5
set xtics (8,32,128,512,"2K" 2048,"8K" 8192,"32K" 32768,"131K" 131072,"524K" 524288) font "Times-Roman,27" offset -0.9,-1
set ytics font "Times-Roman,27"
#set y2tics () font "Times-Roman,25"
set logscale x 2
set linestyle 1 lt 1 pt 70
#set arrow from 72, graph 0 to 72, graph 1 nohead

plot "toplot.txt" u 1:2 t 'LFCA 64' w lp lw 3 lt 8 ps 3 pt 3,\
"" u 1:2:2 with labels center boxed offset 0,2 notitle
